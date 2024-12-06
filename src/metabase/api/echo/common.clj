(ns metabase.api.echo.common
  (:require    [metabase.api.embed.common :refer [normalize-query-params
                                                  validate-and-merge-params]]
               [metabase.api.public :as api.public]
               [metabase.query-processor.card :as qp.card]
               [metabase.query-processor.middleware.constraints :as qp.constraints]
               [metabase.util :as u]))

(set! *warn-on-reflection* true)

(defn- get-embed-dashboard-context
  "If a certain export-format is given, return the correct embedded dashboard context."
  [export-format]
  (case export-format
    "csv"  :embedded-csv-download
    "xlsx" :embedded-xlsx-download
    "json" :embedded-json-download
    :embedded-dashboard))


(defn process-query-for-dashcard
  "Return results for running the query belonging to a DashboardCard. Returns a `StreamingResponse`."
  [& {:keys [dashboard-id dashcard-id card-id export-format embedding-params token-params middleware
             query-params constraints qp database]
      :or   {constraints (qp.constraints/default-query-constraints)
             qp          qp.card/process-query-for-card-default-qp}}]
  {:pre [(integer? dashboard-id) (integer? dashcard-id) (integer? card-id) (u/maybe? map? embedding-params)
         (map? token-params) (map? query-params) (or (nil? database) (integer? database))]}
  (let [resolve-dashboard-parameters (ns-resolve 'metabase.api.embed.common 'resolve-dashboard-parameters)
        slug->value (validate-and-merge-params embedding-params token-params (normalize-query-params query-params))
        parameters  (resolve-dashboard-parameters dashboard-id slug->value)]
    (api.public/process-query-for-dashcard
     :dashboard-id  dashboard-id
     :card-id       card-id
     :dashcard-id   dashcard-id
     :export-format export-format
     :parameters    parameters
     :qp            qp
     :context       (get-embed-dashboard-context export-format)
     :constraints   constraints
     :middleware    middleware
     :database      database)))
