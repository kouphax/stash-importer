(ns stash-importer.core
  (require [clojure.data.csv :as csv]
           [tentacles.issues :as issues]))

(defn- transform
  [row index trans]
  (trans (nth row index)))

(defn- to-technology-map
  [row]
  { :name          (transform row 1 identity)
    :description   (transform row 2 identity)
    :tags          (transform row 3 #(clojure.string/split % #" "))
    :website       (transform row 4 identity)
    :status        (transform row 5 keyword)
    :priority      (transform row 6 #(Boolean/valueOf %))
    :archived      (transform row 7 #(Boolean/valueOf %)) })

(defn- parse-seed-data
  []
  (->> (slurp "resources/seed.csv")
       (csv/read-csv)
       (map to-technology-map)))

(defn- tags []
  (->> (parse-seed-data)
       (map :tags)
       (flatten)
       (distinct)
       (map keyword)))

; .auth is a username:password auth pair dont commit this.
(def credentials (clojure.string/trim (slurp ".auth")))

(defn- remote-tags
  []
  (issues/repo-labels "kouphax" "stash" { :auth credentials }))

(remote-tags)

(parse-seed-data)

(def labels
  { :scala           "F0F8FF"
    :java            "FAEBD7"
    :jvm             "00FFFF"
    :web             "7FFFD4"
    :database        "F0FFFF"
    :persistence     "F5F5DC"
    :testing         "FFE4C4"
    :websockets      "000000"
    :javascript      "FFEBCD"
    :clojure         "0000FF"
    :graphing        "8A2BE2"
    :charting        "A52A2A"
    :dns             "DEB887"
    :ops             "5F9EA0"
    :email           "7FFF00"
    :maven           "D2691E"
    :build-tool      "FF7F50"
    :orm             "6495ED"
    :git             "FFF8DC"
    :version-control "DC143C"
    :templating      "00FFFF"
    :encryption      "00008B"
    :sqlite          "008B8B"
    :jdbc            "B8860B"
    :nodejs          "A9A9A9"
    :documentation   "006400"
    :analytics       "BDB76B"
    :json            "8B008B"
    :android         "556B2F"
    :monitoring      "FF8C00"
    :web-framework   "9932CC"
    :build           "8B0000"
    :xml             "E9967A"
    :compression     "8FBC8F" })

(defn- create-labels
  []
  (doall
    (for [[label colour] labels]
      (issues/create-label "kouphax" "stash" (name label) colour { :auth credentials }))))


(defn- create-issues
  []
  (doall
    (for [tech (parse-seed-data)
          :let [title       (:name tech)
                description (str "[Website](" (:website tech) ")\n\n" (:description tech))
                labels      (map name (:tags tech))]]
      (issues/create-issue "kouphax" "stash" title { :auth credentials :body description :labels labels }))))

