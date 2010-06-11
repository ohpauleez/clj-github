(ns clj-github.core
  (:use [clojure.contrib [string :only [join]] [base64 :only [encode-str]]]
	[clojure-http.client :only [request add-query-params]]
	[org.danlarkin.json :only [decode-from-str]])
  (:import java.net.URI))

(defn create-url [rest user pass]
  (str (if-not (and user pass)
	 (URI. "http" "github.com" (str "/api/v2/json" rest) nil)
	 (URI. "http" (str user ":" pass) "github.com" 80 (str "/api/v2/json" rest) nil nil))))

(def #^{:doc "This var will be rebound to hold authentication information."}
     *authentication* {})

(defn make-request
  "Constructs a basic authentication request."
  [url & {:keys [type data] :or {type "GET" data {}}}]
  (-> (request (add-query-params
		(create-url url
			    (:user *authentication*)
			    (:pass *authentication*))
		data)
	       type)
      :body-seq
      first
      decode-from-str))

(defn handle
  "Checks for an error, and if one has happened, returns the message.
  Otherwise, spits out results."
  ([data sifter] (if-let [result (:error data)] result (sifter data)))
  ([data] (handle data identity)))

(defn slash-join
  "Returns a string of it's arguments separated by forward slashes."
  [& args]
  (join "/" args))

(defn join-up)

(defmacro with-auth [auth body]
  `(binding [*authentication* ~auth] ~body))