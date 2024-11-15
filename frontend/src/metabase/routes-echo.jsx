import { Route } from "react-router";

import { PublicNotFound } from "metabase/public/components/PublicNotFound";
import PublicApp from "metabase/public/containers/PublicApp";

import { PublicOrEmbeddedDashboardPage } from "./public/containers/PublicOrEmbeddedDashboard";

export const getRoutes = store => (
  <Route>
    <Route path="echo" component={PublicApp}>
      <Route
        path="dashboard/:token"
        component={PublicOrEmbeddedDashboardPage}
      />
      <Route path="*" component={PublicNotFound} />
    </Route>
    <Route path="*" component={PublicNotFound} />
  </Route>
);
