(ns slipway.example
  (:require [clojure.test :refer :all]
            [slipway :as slipway]
            [slipway.authz :as authz]
            [slipway.connector.http :as http]
            [slipway.connector.http]
            [slipway.connector.https :as https]
            [slipway.example.app :as app]
            [slipway.handler :as handler]
            [slipway.handler.gzip :as gzip]
            [slipway.server :as server]
            [slipway.session :as session])
  (:import (org.eclipse.jetty.security ConstraintMapping)
           (org.eclipse.jetty.security.authentication BasicAuthenticator FormAuthenticator)
           (org.eclipse.jetty.util.security Constraint)))

(def state (atom nil))

(def constraints
  (let [require-auth (doto (Constraint. "auth" Constraint/ANY_AUTH) (.setAuthenticate true))
        none         (doto (Constraint.) (.setName "no-auth"))]
    [(doto (ConstraintMapping.) (.setConstraint none) (.setPathSpec "/up"))
     (doto (ConstraintMapping.) (.setConstraint none) (.setPathSpec "/css/*"))
     (doto (ConstraintMapping.) (.setConstraint none) (.setPathSpec "/img/*"))
     (doto (ConstraintMapping.) (.setConstraint require-auth) (.setPathSpec "/*"))]))

(def http-connector #::http{:port 3000})

(def https-connector #::https{:port                3443
                              :keystore            "dev-resources/my-keystore.jks"
                              :keystore-type       "PKCS12"
                              :keystore-password   "password"
                              :truststore          "dev-resources/my-truststore.jks"
                              :truststore-password "password"
                              :truststore-type     "PKCS12"})

(def form-authenticator (FormAuthenticator. "/login" "/login-retry" false))

(def options
  {:http          #::server{:connectors    [http-connector]
                            :error-handler app/server-error-handler}

   :https         #::server{:connectors    [https-connector]
                            :error-handler app/server-error-handler}

   :http+https    #::server{:connectors    [http-connector https-connector]
                            :error-handler app/server-error-handler}

   :jaas-auth     #::authz{:realm               "slipway"
                           :login-service       "jaas"
                           :hash-user-file      "common/dev-resources/jaas/hash-realm.properties"
                           :authenticator       form-authenticator
                           :constraint-mappings constraints}

   :hash-auth     #::authz{:realm               "slipway"
                           :login-service       "hash"
                           :hash-user-file      "common/dev-resources/jaas/hash-realm.properties"
                           :authenticator       form-authenticator
                           :constraint-mappings constraints}

   :basic-auth    #::authz{:authenticator (BasicAuthenticator.)}

   :gzip-nil      #::gzip{:enabled? nil}

   :gzip-false    #::gzip{:enabled? false}

   :gzip-true     #::gzip{:enabled? true}

   :custom-ws     #::handler{:ws-path "/wsx"}

   :short-session #::session{:max-inactive-interval 10}

   :join          #::slipway{:join? true}})

(defn stop!
  []
  (when-let [server @state]
    (slipway/stop server)))

"To run a JAAS authenticated server, start a REPL with the following JVM JAAS parameter:
   - Hash User Auth  ->  -Djava.security.auth.login.config=common/dev-resources/jaas/hash-jaas.conf
   - LDAP Auth       ->  -Djava.security.auth.login.config=common/dev-resources/jaas/ldap-jaas.conf

 E.g: (start! [:http :hash-auth :basic-auth])"
(defn start!
  [keys]
  (stop!)
  (reset! state (slipway/start (app/handler) (reduce (fn [ret k] (merge ret (get options k))) {} keys))))