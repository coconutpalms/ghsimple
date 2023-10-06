#!/usr/bin/env bb
(ns ghsimple-dns
  (:require [babashka.http-client :as http]
            [babashka.http-client.interceptors :as interceptors]
            [babashka.cli :as cli]
            [cheshire.core :as json]))

(def json-interceptor
  {:name ::json
   :description
   "A request with `:as :json` will automatically get the
         \"application/json\" accept header and the response is decoded as JSON."
   :request (fn [request]
              (if (= :json (:as request))
                (-> (assoc-in request [:headers :accept] "application/json")
                    ;; Read body as :string
                    ;; Mark request as amenable to json decoding
                    (assoc :as :string ::json true))
                request))
   :response (fn [response]
               (if (get-in response [:request ::json])
                 (update response :body #(json/parse-string % true))
                 response))})
        ;; Add json interceptor add beginning of chain
        ;; It will be the first to see the request and the last to see the response
(def interceptors (cons json-interceptor interceptors/default-interceptors))

(def github-api-key (System/getenv "GITHUB_TOKEN"))
(def dnsimple-api-token (System/getenv "DNSIMPLE_TOKEN"))

(def opts (cli/parse-opts
           *command-line-args*
           {:require [:gh-branch :gh-username :gh-repo-name :domain-name]}))

(def github-branch (:gh-branch opts))
(def github-username (:gh-username opts))
(def github-repo-name (:gh-repo-name opts))
(def github-repository (str github-username "/" github-repo-name))
(def domain-name (:domain-name opts))

;; (println opts)

(defn configure-repo-for-pages []
  (let [url (str "https://api.github.com/repos/" github-repository "/pages")
        headers {"Authorization" (str "token " github-api-key)
                 "Accept" "application/vnd.github.v3+json"}
        body {:source {:branch github-branch :path "/"}}
        response (http/post url {:headers headers :body (json/generate-string body) :as :json :interceptors interceptors})]
    (if (#{201 204} (:status response))
      (println "Repository successfully configured for GitHub Pages.")
      (do (println "Failed to configure the repository for GitHub Pages.")
          (println "Response:" (:status response) (:body response))))))

(defn add-cname-file []
  (let [url (str "https://api.github.com/repos/" github-repository "/contents/CNAME")
        headers {"Authorization" (str "token " github-api-key)
                 "Accept" "application/vnd.github.v3+json"}
        encoded-domain (.encodeToString (java.util.Base64/getEncoder) (.getBytes domain-name))
        body {:message "Add CNAME file"
              :content encoded-domain}

        response (http/put url {:headers headers :body (json/generate-string body) :as :json :interceptors interceptors})]
    (if (#{200 201 204} (:status response))
      (println "CNAME file successfully added.")
      (do (println "Failed to add CNAME file.")
          (println "Response:" (:status response) (:body response))))))

(configure-repo-for-pages)
(add-cname-file)

(def dnsimple-account-id
  (->
   (http/get "https://api.dnsimple.com/v2/accounts"
             {:headers {:Authorization (str "Bearer " dnsimple-api-token)}
              :as :json :interceptors interceptors})
   :body :data first :id))

(println "account: " dnsimple-account-id)

(defn get-domain-id []
  (let [url (str "https://api.dnsimple.com/v2/" dnsimple-account-id "/domains/" domain-name)
        headers {:Authorization (str "Bearer " dnsimple-api-token)}
        response (http/get url {:headers headers :as :json :interceptors interceptors})]
    (if (= 200 (:status response))
      (:id (:data (:body response)))
      (do (println "Failed to get domain ID.")
          (println "Response:" (:body response))
          nil))))

(defn apply-github-pages-service [domain-id]
  (let [url (str "https://api.dnsimple.com/v2/" dnsimple-account-id "/domains/" domain-id "/services/github-pages")
        headers {"Authorization" (str "Bearer " dnsimple-api-token)
                 "Content-type" "application/json"
                 "Accept" "application/json"}
        body {:settings {:github_name github-username}}
        #_#__ (do
                (println "URL:" url)
                (println "Headers:" headers)
                (println "Body:" body))
        response (http/post url {:headers headers :body (json/generate-string body)
                                 :as :json :interceptors interceptors})]
    (if (#{200 201 204} (:status response))
      (do
        (println "GitHub Pages service successfully applied to domain.")
        #_(println response))
      (do (println "Failed to apply GitHub Pages service to domain.")
          (println "Response:" (:body response))))))

(def domain-id (get-domain-id))
(println "Domain id: " domain-id)
(if domain-id
  (apply-github-pages-service domain-id)
  (println "ERROR domain id lookup failed"))
