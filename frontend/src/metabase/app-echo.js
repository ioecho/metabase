import { isWithinIframe } from "metabase/lib/dom";

import { init } from "./app";
import { publicReducers } from "./reducers-public";
import { getRoutes } from "./routes-echo";

init(publicReducers, getRoutes, () => {
  if (isWithinIframe()) {
    document.body.style.backgroundColor = "transparent";
  }
});
