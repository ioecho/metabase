(ns metabase.api.echo
  (:require
   [compojure.core :refer [GET]]
   [metabase.api.common :as api]
   [metabase.api.echo.common :as api.echo.common]
   [metabase.api.embed :refer [unsign-and-translate-ids]]
   [metabase.api.embed.common :as api.embed.common]
   [metabase.events :as events]
   [metabase.models.dashboard :refer [Dashboard]]
   [metabase.query-processor.card :as qp.card]
   [metabase.query-processor.middleware.constraints :as qp.constraints]
   [metabase.util :as u]
   [metabase.util.embed :as embed]
   [metabase.util.malli.schema :as ms]
   [toucan2.core :as t2]))

(set! *warn-on-reflection* true)

;;; ----------------------------------------- /api/embed/dashboard endpoints -----------------------------------------

(defn- process-query-for-dashcard-with-signed-token
  "Fetch the results of running a Card belonging to a Dashboard using a JSON Web Token signed with the
   `embedding-secret-key`.

   [[Token]] should have the following format:

     {:resource {:dashboard <dashboard-id>}
      :params   <parameters>}

  Additional dashboard parameters can be provided in the query string, but params in the JWT token take precedence.

  Returns a `StreamingResponse`."
  [token dashcard-id card-id export-format query-params
   & {:keys [constraints qp middleware]
      :or   {constraints (qp.constraints/default-query-constraints)
             qp          qp.card/process-query-for-card-default-qp}}]
  (let [unsigned-token (unsign-and-translate-ids token)
        dashboard-id   (embed/get-in-unsigned-token-or-throw unsigned-token [:resource :dashboard])
        database       (get-in unsigned-token [:resource :database] nil)]
    (api.embed.common/check-embedding-enabled-for-dashboard dashboard-id)
    (api.echo.common/process-query-for-dashcard
     :export-format    export-format
     :dashboard-id     dashboard-id
     :dashcard-id      dashcard-id
     :card-id          card-id
     :embedding-params (t2/select-one-fn :embedding_params Dashboard :id dashboard-id)
     :token-params     (embed/get-in-unsigned-token-or-throw unsigned-token [:params])
     :query-params     (api.embed.common/parse-query-params (dissoc query-params :format_rows :pivot_results))
     :constraints      constraints
     :qp               qp
     :middleware       middleware
     :database         database)))

(api/defendpoint GET "/dashboard/:token/dashcard/:dashcard-id/card/:card-id"
  "Fetch the results of running a Card belonging to a Dashboard using a JSON Web Token signed with the
  `embedding-secret-key`"
  [token dashcard-id card-id & query-params]
  {dashcard-id ms/PositiveInt
   card-id     ms/PositiveInt}
  (u/prog1 (process-query-for-dashcard-with-signed-token token dashcard-id card-id :api
                                                         (api.embed.common/parse-query-params query-params))
    (events/publish-event! :event/card-read {:object-id card-id, :user-id api/*current-user-id*, :context :dashboard})))

;;; ----------------------------------------------- Param values -------------------------------------------------

(api/defendpoint GET "/dashboard/:token/params/:param-key/values"
  "Embedded version of chain filter values endpoint."
  [token param-key :as {:keys [query-params]}]
  (api.embed.common/dashboard-param-values token param-key nil
                                           (api.embed.common/parse-query-params query-params)))

(api/define-routes)
