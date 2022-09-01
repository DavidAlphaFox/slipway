(ns slipway.auth
  (:require [clojure.core.protocols :as p]
            [clojure.tools.logging :as log])
  (:import (java.util List)
           (org.eclipse.jetty.server Authentication$User)
           (javax.security.auth.login Configuration)        ;; Jetty9/10/11 all use javax in this specific case.
           (org.eclipse.jetty.jaas JAASLoginService)
           (org.eclipse.jetty.security ConstraintSecurityHandler HashLoginService LoginService)
           (org.eclipse.jetty.security.authentication BasicAuthenticator FormAuthenticator)
           (org.eclipse.jetty.server Authentication$User Request)))

(defmulti login-service :auth-type)

(defn user
  "Derive user identity from a jetty base request"
  [^Request base-request]
  (when-let [authentication (.getAuthentication base-request)]
    (when (instance? Authentication$User authentication)
      (p/datafy authentication))))

(defn logout
  "Logout user and invalidate the session"
  [{:keys [slipway.user/identity ^Request slipway.handler/base-request]}]
  (try
    (log/debug "logout" identity)
    (.logout base-request)
    (.invalidate (.getSession base-request))
    (catch Exception ex
      (log/error ex "logout error"))))

(defmethod login-service "jaas"
  [{:keys [realm]}]
  (let [config (System/getProperty "java.security.auth.login.config")]
    (log/infof "initializing JAASLoginService -> realm: %s, java.security.auth.login.config: %s " realm config)
    (if config
      (when (slurp config)                                  ;; biffs an exception if not found
        (doto (JAASLoginService. realm) (.setConfiguration (Configuration/getConfiguration))))
      (throw (ex-info (str "start with -Djava.security.auth.login.config=/some/path/to/jaas.config to use Jetty/JAAS auth provider") {})))))

(defmethod login-service "hash"
  [{:keys [hash-user-file realm]}]
  (log/infof "initializing HashLoginService -> realm: %s, realm file: %s" realm hash-user-file)
  (if hash-user-file
    (when (slurp hash-user-file)
      (HashLoginService. realm hash-user-file))
    (throw (ex-info (str "set the path to your hash user realm properties file") {}))))

(defn handler
  [^LoginService login-service {:keys [auth-method login-uri login-retry-uri constraint-mappings]}]
  (doto (ConstraintSecurityHandler.)
    (.setConstraintMappings ^List constraint-mappings)
    (.setAuthenticator (if (= "basic" auth-method)
                         (BasicAuthenticator.)
                         (FormAuthenticator. login-uri login-retry-uri false)))
    (.setLoginService login-service)))