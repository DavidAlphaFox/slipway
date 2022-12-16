# Slipway: a Clojure companion to Jetty

[![Slipway Test](https://github.com/operatr-io/slipway/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/operatr-io/slipway/actions/workflows/ci.yml)

[Eclipse Jetty](https://www.eclipse.org/jetty/) is the web server at the heart of our product, [Kpow for Apache Kafka®](https://kpow.io).

Slipway is our [Clojure](https://clojure.org/) companion to embedded Jetty.

Slipway provides access to a battle-tested web server with websocket support.

### Prior Art

Slipway is based on and in some cases includes code from the following projects:

* [sunng87/ring-jetty9-adapter](https://github.com/sunng87/ring-jetty9-adapter) by [Ning Sun](https://github.com/sunng87)
* [ring-clojure/ring](https://github.com/ring-clojure/ring/tree/master/ring-jetty-adapter) by [James Reeves](https://github.com/weavejester)

We appreciate the great open-source work of Ning and James that allowed us to build our initial product.

### Quick Start

Choose a project by Jetty version, then open a REPL.

Start slipway with a ring-handler and a map of configuration options:

```clojure 
(require '[slipway :as slipway])
(require '[slipway.server :as server])
(require '[slipway.connector.http :as http])

(defn handler [_] {:status 200 :body "Hello world"})

(def http-connector #::http{:port 3000})

(slipway/start handler #::server{:connectors [http-connector]})
```

Your hello world application is now running on [http://localhost:3000](http://localhost:3000).

### Example Configurations

Various configuration of Slipway can be found in the [example.clj](common/test/slipway/example.clj) namespace.

These configurations are used by our integration tests. The stateful start!/stop! functions within that namespace are not considered a guide for your own use of Slipway, but they are convenient for playing with server configurations like so:

```clojure
(require '[slipway.example :as example])

(example/start! [:http :hash-auth :short-session])
```

Your sample application with [property file based authz](https://docs.kpow.io/authentication/file/) is now available on [http://localhost:3000](http://localhost:3000).

You can login with jetty/jetty, admin/admin, plain/plain, other/other, or user/password as defined in [hash-realm.properties](common/dev-resources/jaas/hash-realm.properties).

Thanks to the `:short-session` configuration your session will expire after 10s of inactivity.

-----

![Slipway Login](docs/img/slipway-login.png)

## Why Jetty?

Jetty is a mature, stable, commercially supported project with an [active, deeply experienced](https://github.com/eclipse/jetty.project/graphs/contributors) core team of contributors.

Ubiquitous in the enterprise Java world, Jetty has many eyes raising issues and driving improvments.

More than a simple web server, Jetty is battle-tested, performant, and feature rich.

## Our Requirements

Kpow is a secure web-application with a rich SPA UI served by websockets. 

Deployed in-cloud and on-premises Kpow has seemingly every possible Jetty configuration option in use by at least one end-user.

> User: Can I configure a custom CA certificate to secure my JAAS/LDAPS authentication?

> Kpow Team: Yes (thanks to Jetty).

We have a hard requirement to support customers on Java 8 and Java 11+ and incorporate feedback from external security teams.

## Primary Goals

Slipway aims to provide first-class, extensible support for: 

* HTTP 1.1
* HTTPS / SSL
* Synchronous handlers
* JAAS Authentication (LDAP, HashUser, etc)
* Form / basic authentication
* WebSockets
* Java 8 / 11+
* Jetty 9 / 10 / 11
* Session management
* Proxy protocol / http forwarded
* Common / sensible defaults (e.g. gzip compression)
* Configurable error handling
* Automated CVE scanning with NVD
* Comprehensive integration tests
* Ring compatibility

## Secondary Goals

* Broad support for general Jetty use-cases / configuration

## Future Goals

* Backport our SAML, OpenID and OAuth authentication implementations
* Open-source a full-stack example application using slipway in [shortcut](https://github.com/factorhouse/shortcut).

## Currently Out Of Scope

* Http2/3
* Asynchronous Handlers
* Ajax (including auto-fallback)

## Non-Goals

* A simplified DSL for Jetty

## Installation

| Jetty Version | Clojars Project |
| ------------- | --------------- |
| Jetty 9 | [![Clojars Project](https://img.shields.io/clojars/v/io.factorhouse/slipway-jetty9.svg)](https://clojars.org/io.factorhouse/slipway-jetty9) |
| Jetty 10 | [![Clojars Project](https://img.shields.io/clojars/v/io.factorhouse/slipway-jetty10.svg)](https://clojars.org/io.factorhouse/slipway-jetty10) |
| Jetty 11 | [![Clojars Project](https://img.shields.io/clojars/v/io.factorhouse/slipway-jetty11.svg)](https://clojars.org/io.factorhouse/slipway-jetty11) |

* Jetty 9: If you require running with Java 8
* Jetty 10: Recommended for general use, requires Java 11+
* Jetty 11: If you want to run with Jakarta rather than Javax, requires Java 11+

### Configuration

TBD: short-term check out [slipway.clj](common/src/slipway.clj) for options configuration and [example.clj](common/test/slipway/example.clj) for example usage.

```clojure
  #:slipway.handler.gzip{:enabled?            "is gzip enabled? default true"
                         :included-mime-types "mime types to include (without charset or other parameters), leave nil for default types"
                         :excluded-mime-types "mime types to exclude (replacing any previous exclusion set)"
                         :min-gzip-size       "min response size to trigger dynamic compression (in bytes, default 1024)"}

  #:slipway.connector.https{:host                       "the network interface this connector binds to as an IP address or a hostname.  If null or 0.0.0.0, then bind to all interfaces. Default null/all interfaces"
                            :port                       "port this connector listens on. If set to 0 a random port is assigned which may be obtained with getLocalPort(), default 443"
                            :idle-timeout               "max idle time for a connection, roughly translates to the Socket.setSoTimeout. Default 200000 ms"
                            :http-forwarded?            "if true, add the ForwardRequestCustomizer. See Jetty Forward HTTP docs"
                            :proxy-protocol?            "if true, add the ProxyConnectionFactor. See Jetty Proxy Protocol docs"
                            :http-config                "a concrete HttpConfiguration object to replace the default config entirely"
                            :configurator               "a fn taking the final connector as argument, allowing further configuration"
                            :keystore                   "keystore to use, either path (String) or concrete KeyStore"
                            :keystore-type              "type of keystore, e.g. JKS"
                            :keystore-password          "password of the keystore"
                            :key-manager-password       "password for the specific key within the keystore"
                            :truststore                 "truststore to use, either path (String) or concrete KeyStore"
                            :truststore-password        "password of the truststore"
                            :truststore-type            "type of the truststore, eg. JKS"
                            :include-protocols          "a list of protocol name patterns to include in SSLEngine"
                            :exclude-protocols          "a list of protocol name patterns to exclude from SSLEngine"
                            :replace-exclude-protocols? "if true will replace existing exclude-protocols, otherwise will add them"
                            :exclude-ciphers            "a list of cipher suite names to exclude from SSLEngine"
                            :replace-exclude-ciphers?   "if true will replace existing exclude-ciphers, otherwise will add them"
                            :security-provider          "the security provider name"
                            :client-auth                "either :need or :want to set the corresponding need/wantClientAuth field"
                            :ssl-context                "a concrete pre-configured SslContext"
                            :sni-required?              "true if a SNI certificate is required, default false"
                            :sni-host-check?            "true if the SNI Host name must match, default false"}

  #:slipway.connector.http{:host            "the network interface this connector binds to as an IP address or a hostname.  If null or 0.0.0.0, then bind to all interfaces. Default null/all interfaces."
                           :port            "port this connector listens on. If set to 0 a random port is assigned which may be obtained with getLocalPort(), default 80"
                           :idle-timeout    "max idle time for a connection, roughly translates to the Socket.setSoTimeout. Default 200000 ms"
                           :http-forwarded? "if true, add the ForwardRequestCustomizer. See Jetty Forward HTTP docs"
                           :proxy-protocol? "if true, add the ProxyConnectionFactory. See Jetty Proxy Protocol docs"
                           :http-config     "a concrete HttpConfiguration object to replace the default config entirely"
                           :configurator    "a fn taking the final connector as argument, allowing further configuration"}

  #:slipway.authz{:realm               "the Jetty authentication realm"
                  :hash-user-file      "the path to a Jetty Hash User File"
                  :login-service       "pluggable Jetty LoginService identifier, 'jaas' and 'hash' supported by default"
                  :authenticator       "a concrete Jetty Authenticator (e.g. FormAuthenticator or BasicAuthenticator)"
                  :constraint-mappings "a list of concrete Jetty ConstraintMapping"}

  #:slipway.session{:secure-request-only?  "set the secure flag on session cookies (default true)"
                    :http-only?            "set the http-only flag on session cookies (default true)"
                    :same-site             "set session cookie same-site policy to :none, :lax, or :strict (default :strict)"
                    :max-inactive-interval "max session idle time (in s, default -1)"
                    :tracking-modes        "a set (colloection) of #{:cookie, :ssl, or :url}"
                    :cookie-name           "the name of the session cookie"
                    :session-id-manager    "the meta manager used for cross context session management"
                    :refresh-cookie-age    "max time before a session cookie is re-set (in s)"
                    :path-parameter-name   "name of path parameter used for URL session tracking"}

  ;; Jetty 10 / Jetty 11 Websockets
  #:slipway.websockets{:idle-timeout            "max websocket idle time (in ms), default 500000"
                       :input-buffer-size       "max websocket input buffer size (in bytes)"
                       :output-buffer-size      "max websocket output buffer size (in bytes)"
                       :max-text-message-size   "max websocket text message size (in bytes, default 65536)"
                       :max-binary-message-size "max websocket binary message size (in bytes)"
                       :max-frame-size          "max websocket frame size (in bytes)"
                       :auto-fragment           "websocket auto fragment (boolean)"}

  ;; Jetty 9 Websockets
  #:slipway.websockets{:idle-timeout            "max websocket idle time (in ms), default 500000"
                       :input-buffer-size       "max websocket input buffer size"
                       :max-text-message-size   "max websocket text message size"
                       :max-binary-message-size "max websocket binary message size"}

  #:slipway.handler{:context-path    "the root context path, default '/'"
                    :ws-path         "the path serving the websocket upgrade handler, default '/chsk'"
                    :null-path-info? "true if /path is not redirected to /path/, default true"}

  #:slipway.server{:handler       "the base Jetty handler implementation (:default defmethod impl found in slipway.handler)"
                   :connectors    "the connectors supported by this server"
                   :thread-pool   "the thread-pool used by this server (leave null for reasonable defaults)"
                   :error-handler "the error-handler used by this server for Jetty level errors"}

  #:slipway{:join? "join the Jetty threadpool, blocks the calling thread until jetty exits, default false"}
```

### TBD Update Below This Line
----

### WebSockets

Slipway provides the same API as the [ring-jetty9-adapter](https://github.com/sunng87/ring-jetty9-adapter) for upgrading HTTP requests to WebSockets. 

```clojure 
(require '[slipway.websockets :as ws])
(require '[slipway.server :as slipway])

(def ws-handler {:on-connect (fn [ws] (ws/send! ws "Hello world"))
                 :on-error (fn [ws e])
                 :on-close (fn [ws status-code reason])
                 :on-text (fn [ws text-message])
                 :on-bytes (fn [ws bytes offset len])
                 :on-ping (fn [ws bytebuffer])
                 :on-pong (fn [ws bytebuffer])})

(defn handler [req]
  (if (ws/upgrade-request? req)
    (ws/upgrade-response ws-handler)
    {:status 406}))
    
(slipway/run-jetty handler {:port 3000 :join? false})
```

The `ws` object passed to each handler function implements the `slipway.websockets.WebSockets` protocol:

```clojure 
(defprotocol WebSockets
  (send! [this msg] [this msg callback])
  (ping! [this] [this msg])
  (close! [this] [this status-code reason])
  (remote-addr [this])
  (idle-timeout! [this ms])
  (connected? [this])
  (req-of [this]))
```

#### Sente Integration

Slipway supports [Sente](https://github.com/ptaoussanis/sente) out-of-the box. 

Simply include Sente in your project's dependencies and follow Sente's [getting started guide](https://github.com/ptaoussanis/sente#getting-started), and use the slipway web-server adapter:

```clojure 
(require '[slipway.sente :refer [get-sch-adapter]])
```

### JAAS integration

JAAS implements a Java version of the standard Pluggable Authentication Module (PAM) framework.

JAAS can be used for two purposes:

* for authentication of users, to reliably and securely determine who is currently executing Java code, regardless of whether the code is running as an application, an applet, a bean, or a servlet; and
* for authorization of users to ensure they have the access control rights (permissions) required to do the actions performed.

JAAS implements a Java version of the standard Pluggable Authentication Module (PAM) framework. See Making Login Services Independent from Authentication Technologies for further information.

For more information visit the [Jetty documentation](https://www.eclipse.org/jetty/documentation/jetty-10/operations-guide/index.html#og-jaas).

Slipway is the only ring adapter that supports Jetty JAAS out of the box. Thus, one of the few ways to authenticate using LDAP in the Clojure world. Oftentimes a requirement for the enterprise.

#### Usage

Pass an `:auth` key to your `run-jetty` options map:

```clojure 
(require '[slipway.auth.constraints :as constraints])

{:auth-method         "basic"                               ;; either "basic" (basic authentication) or "form" (form based authencation, with a HTML login form served at :login-uri)
 :auth-type           "jaas"                                ;; either "jaas" or "hash"
 :login-uri           "/login"                              ;; the URI where the login form is hosted
 :login-retry-uri     "/login-retry"
 :realm               "my-app"
 :logout-uri          "/logout"
 :session             {:http-only?            true
                       :same-site             :strict       ;; can be :lax, :strict or :none
                       :tracking-modes        #{:cookie}    ;; can be :url, :cookie :ssl
                       :max-inactive-interval -1}           ;; set the max period of inactivity, after which the session is invalidated, in seconds.
 :constraint-mappings (constraints/constraint-mappings
                       ;; /css/* is not protected. Everyone (including unauthenticated users) can access
                       ["/css/*" (constraints/no-auth)]
                       ;; /api/* is protected. Any authenticated user can access
                       ["/api/*" (constraints/basic-auth-any-constraint)])}
```

Successfully authenticated users will have their details assoced into the Ring request map under the key `:slipway.auth/user` - it contains: 

```clojure
{:provider :jetty
 :name     "Jane"
 :roles    ["admin"]}
```

#### Constraints

[Constraints](https://www.eclipse.org/jetty/javadoc/jetty-10/org/eclipse/jetty/util/security/Constraint.html) describe an auth and/or data constraint. 

The `slipway.auth.constraints` namespace has a few useful helper functions for working with constraints. 

#### jaas.config

Start your application (JAR or REPL session) with the additional JVM opt `-Djava.security.auth.login.config=/some/path/to/jaas.config`

For example configurations refer to [this tutorial](https://wiki.eclipse.org/Jetty/Tutorial/JAAS#Configuring_a_JAASLoginService)

#### Hash realm authentication

The simplest JAAS authentication module. A static list of hashed users in a file. 

Example `jaas.config`: ('my-app' must be the same as the configured :realm)

``` 
my-app {
           org.eclipse.jetty.jaas.spi.PropertyFileLoginModule required
           debug="true"
           file="dev-resources/jaas/hash-realm.properties";
       };
```

Example `hash-realm.properties`:

```
# This file defines users passwords and roles for a HashUserRealm
#
# The format is
#  <username>: <password>[,<rolename> ...]
#
# Passwords may be clear text, obfuscated or checksummed.  The class
# org.eclipse.jetty.util.security.Password should be used to generate obfuscated
# passwords or password checksums
#
# If DIGEST Authentication is used, the password must be in a recoverable
# format, either plain text or OBF:.
#
jetty: MD5:164c88b302622e17050af52c89945d44,kafka-users,content-administrators
admin: CRYPT:adpexzg3FUZAk,server-administrators,content-administrators,kafka-admins
other: OBF:1xmk1w261u9r1w1c1xmq,kafka-admins,kafka-users
plain: plain,content-administrators
user: password,kafka-users
# This entry is for digest auth.  The credential is a MD5 hash of username:realmname:password
digest: MD5:6e120743ad67abfbc385bc2bb754e297,kafka-users
```

#### LDAP authentication

Example `jaas.config`:

``` 
ldaploginmodule {
   org.eclipse.jetty.plus.jaas.spi.LdapLoginModule required
   debug="true"
   contextFactory="com.sun.jndi.ldap.LdapCtxFactory"
   hostname="ldap.example.com"
   port="389"
   bindDn="cn=Directory Manager"
   bindPassword="directory"
   authenticationMethod="simple"
   forceBindingLogin="false"
   userBaseDn="ou=people,dc=alcatel"
   userRdnAttribute="uid"
   userIdAttribute="uid"
   userPasswordAttribute="userPassword"
   userObjectClass="inetOrgPerson"
   roleBaseDn="ou=groups,dc=example,dc=com"
   roleNameAttribute="cn"
   roleMemberAttribute="uniqueMember"
   roleObjectClass="groupOfUniqueNames";
   };
```

## Examples

Check back soon! 

## License

Distributed under the MIT License.

Copyright (c) 2022 Factor House
