(ns meilisearch.client
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [config.core :as config])
  (:import [java.net ConnectException]))

(def server-url (:meilisearch-url config/env "http://localhost:7700"))

(defn- api-call [uri params]
  (let [endpoint (format "%s/%s" server-url uri)]
    (http/post endpoint
               {:body (json/generate-string params)
                :content-type :json
                :as :json
                :throw-exceptions true
                :ignore-unknown-host false
                ;; ignore index not found
                :unexceptional-status #(or (<= 200 % 299)
                                           (= % 404))})))

(defn create-index
  "uid is case insensistive, prefer lower case."
  [uid]
  (api-call "indexes" {:uid uid}))

(defn add-documents [uid docs]
  (let [uri (format "indexes/%s/documents" uid)]
    (api-call uri docs)))

(defn delete-documents [uid docs]
  (let [uri (format "indexes/%s/documents/delete-batch" uid)]
    (api-call uri docs)))

(defn search-documents
  "Check https://docs.meilisearch.com/references/search.html#body
   for search params detail.

  {:q \"sample query term\"
   :offset 0
   :limit 20}"
  [uid params]
  (let [uri (format "indexes/%s/search" uid)]
    (:body (api-call uri params))))