(ns slipway.server
  "A Jetty9 server that conforms to the slipway API, inspired by:
    * https://github.com/sunng87/ring-jetty9-adapter/blob/master/src/ring/adapter/jetty9.clj
    * https://github.com/ring-clojure/ring/blob/master/ring-jetty-adapter/src/ring/adapter/jetty.clj"
  (:require [clojure.tools.logging :as log]
            [slipway.auth :as auth]
            [slipway.common.auth :as common.auth]
            [slipway.common.server :as common.server]
            [slipway.common.servlet :as common.servlet]
            [slipway.common.websockets :as common.ws]
            [slipway.websockets :as ws])
  (:import (javax.servlet.http HttpServletRequest HttpServletResponse)
           (org.eclipse.jetty.server Handler Request Server)
           (org.eclipse.jetty.server.handler AbstractHandler ContextHandler HandlerList)))

;(defn safe-login-redirect
;  "When logging in we have some special cases to consider with the post-login uri"
;  [target request {:keys [login-uri login-retry-uri]}]
;  (when (#{login-uri login-retry-uri} target)
;    (let [post-login-uri (.getAttribute (.getSession request) FormAuthenticator/__J_URI)]
;      (when (.contains post-login-uri "/chsk")
;        (log/info "avoiding /chsk post-login, setting post-login uri to '/'")
;        (.setAttribute (.getSession request) FormAuthenticator/__J_URI "/")))))

(defn handle-http
  [handler request-map base-request response]
  (try
    (let [request-map  (assoc request-map
                              ::auth/user (common.auth/user base-request)
                              ::request base-request)
          response-map (handler request-map)]
      (when response-map
        (if (common.ws/upgrade-response? response-map)
          (common.servlet/update-servlet-response response {:status 406})
          (common.servlet/update-servlet-response response response-map))))
    (catch Throwable e
      (log/error e "unhandled exception processing HTTP request")
      (.sendError response 500 (.getMessage e)))
    (finally
      (.setHandled base-request true))))

(defn proxy-handler
  [handler]
  (proxy [AbstractHandler] []
    (handle [_ ^Request base-request ^HttpServletRequest request ^HttpServletResponse response]
      (try
        (let [request-map (common.servlet/build-request-map request)]
          (if (common.ws/upgrade-request? request-map)
            (.setHandled base-request false)
            (handle-http handler request-map base-request response)))
        (catch Throwable e
          (log/error e "unhandled exception processing HTTP request")
          (.sendError response 500 (.getMessage e))
          (.setHandled base-request true))))))

(defn start-jetty
  "Starts a Jetty server.
   See https://github.com/factorhouse/slipway#usage for list of options"
  ^Server [handler {:as   options
                    :keys [configurator join? auth gzip? gzip-content-types gzip-min-size http-forwarded? error-handler]
                    :or   {gzip? true}}]
  (log/info "configuring Jetty9")
  (let [server           (common.server/create-server options)
        ring-app-handler (proxy-handler handler)
        ws-handler       (ws/proxy-ws-handler handler options)
        contexts         (doto (HandlerList.)
                           (.setHandlers (into-array Handler [ring-app-handler ws-handler])))]
    (.setHandler server contexts)
    (when configurator (configurator server))
    (when http-forwarded? (common.server/add-forward-request-customizer server))
    (when gzip? (common.server/enable-gzip-compression server gzip-content-types gzip-min-size))
    (when error-handler (.setErrorHandler server error-handler))
    (when auth (common.auth/configure server auth))

    ;; TODO invert the above functions to work on handlers not servers
    ;; TODO figure out importance of order of handlers
    (let [handler (.getHandler server)]
      (.setHandler server (doto (ContextHandler.)
                            (.setContextPath "/")
                            (.setAllowNullPathInfo true)
                            (.setHandler handler))))

    (.start server)
    (when join? (.join server))
    server))

(defn stop-jetty
  [^Server server]
  (.stop server))