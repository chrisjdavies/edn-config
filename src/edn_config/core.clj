(ns edn-config.core
  (:require [clojure.edn :as edn]
            [environ.core :refer [env]]
            [medley.core :refer [map-vals]])
  (:refer-clojure :exclude [load-file load-string]))

(defn- maybe-assoc-env
  "If there is a value for v in the environment then assoc k with that
  value in m.  Else just return m."
  [m [k v]]
  (if-let [env-val (env v)]
    (assoc m k env-val)
    m))

(defn- process-env-map
  "For every kv-pair in env-map, return a new map of successful
  environment mappings."
  [env-map]
  (reduce maybe-assoc-env {} env-map))

(defn- only-map
  "Return v if it's a map or nil otherwise."
  [v]
  (when (map? v)
    v))

(defn merge-env
  "If given a map with the :env key mapping to another map, returns
  the input merged with the results of doing an environment-lookup for
  that value.

  Best demonstrated with an example:

    {:username \"test\"
     :password \"test\"
     :env {:username :prod-username
           :password :prod-password}}

  When passed this structure, merge-env will replace :username with
  the value of :prod-username in the environment, if a value exists,
  and :password with the environment value of :prod-password.

  If no values are found in the environment, the original mappings are
  used.

  merge-env is recursive; it will walk all sub-maps, too.  This allows
  for configurations such as:

    {:database {:username \"test\"
                :password \"test\"
                :env {:username :db-username
                      :password :db-password}}
     :backups {:path \"/tmp/backups\"
               :env {:path :backups-path}}}"
  [opts]
  (if (map? opts)
    (merge (map-vals merge-env opts)
           (some-> opts :env only-map process-env-map))
    opts))

(defn load-string
  "Load a configuration string in EDN form.  If the top-level form is a
  map, process any :env sub-maps with values from the environment.

  See merge-env for how env-mapping works."
  [config-str]
  (some-> config-str
          edn/read-string
          merge-env))

(defn load-file
  "Load a configuration file.  If the top-level form is a map, process
  any :env sub-maps with values from the environment.

  See merge-env for how env-mapping works."
  [path]
  (-> path slurp load-string))
