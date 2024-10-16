import cx from "classnames";
import type { MouseEvent } from "react";
import { forwardRef, useCallback, useMemo } from "react";
import { t } from "ttag";

import CS from "metabase/css/core/index.css";
import DashboardS from "metabase/css/dashboard.module.css";
import EmbedFrameS from "metabase/public/components/EmbedFrame/EmbedFrame.module.css";
import { Icon, Select } from "metabase/ui";
import { HARD_ROW_LIMIT } from "metabase-lib/v1/queries/utils";

import {
  PaginationButton,
  PaginationMessage,
  TableFooterRoot,
} from "./TableSimple.styled";

interface TableFooterProps {
  className?: string;
  "data-testid"?: string;
  start: number;
  end: number;
  total: number;
  limit?: number;
  onPreviousPage: () => void;
  onNextPage: () => void;
  singleItem?: boolean;
  jumpPage?: (page: number) => void;
}

const TableFooter = forwardRef<HTMLDivElement, TableFooterProps>(
  function TableFooter(
    {
      className,
      "data-testid": dataTestId = "TableFooter",
      start,
      end,
      limit,
      total,
      onPreviousPage,
      onNextPage,
      singleItem,
      jumpPage,
    }: TableFooterProps,
    ref,
  ) {
    const paginateMessage = useMemo(() => {
      const isOverLimit = limit === undefined && total >= HARD_ROW_LIMIT;

      if (singleItem) {
        return isOverLimit
          ? t`Item ${start + 1} of first ${total}`
          : t`Item ${start + 1} of ${total}`;
      }

      return isOverLimit
        ? t`Rows ${start + 1}-${end + 1} of first ${total}`
        : t`Rows ${start + 1}-${end + 1} of ${total}`;
    }, [total, start, end, limit, singleItem]);

    const currentPage = useMemo(() => {
      return Math.ceil(start / (end + 1 - start));
    }, [start, end]);

    const handlePreviousPage = useCallback(
      (event: MouseEvent) => {
        event.preventDefault();
        onPreviousPage();
      },
      [onPreviousPage],
    );

    const handleNextPage = useCallback(
      (event: MouseEvent) => {
        event.preventDefault();
        onNextPage();
      },
      [onNextPage],
    );

    return (
      <TableFooterRoot
        className={cx(
          className,
          DashboardS.fullscreenNormalText,
          DashboardS.fullscreenNightText,
          EmbedFrameS.fullscreenNightText,
        )}
        data-testid={dataTestId}
        ref={ref}
      >
        <PaginationMessage>
          <div style={{ display: "flex", alignItems: "center" }}>
            <div>{paginateMessage}</div>
            <Select
              name="jump-page"
              value={`${currentPage}`}
              onChange={value => {
                if (!jumpPage) {
                  return;
                }
                jumpPage(Number(value));
              }}
              data={
                Array.from(
                  {
                    length:
                      total / (end + 1 - start) +
                      (total % (end + 1 - start) > 0 ? 1 : 0),
                  },
                  (_, i) => i + 1,
                ).map(page => ({
                  value: (page - 1).toString(),
                  label: `${(page - 1) * (end + 1 - start) + 1}~${Math.min(page * (end + 1 - start), total)}`,
                })) ?? []
              }
              style={{ margin: "0 1rem" }}
            />
          </div>
        </PaginationMessage>
        <PaginationButton
          className={CS.textPrimary}
          aria-label={t`Previous page`}
          direction="previous"
          onClick={handlePreviousPage}
          disabled={start === 0}
        >
          <Icon name="chevronleft" />
        </PaginationButton>
        <PaginationButton
          className={CS.textPrimary}
          aria-label={t`Next page`}
          direction="next"
          onClick={handleNextPage}
          disabled={end + 1 >= total}
        >
          <Icon name="chevronright" />
        </PaginationButton>
      </TableFooterRoot>
    );
  },
);

// eslint-disable-next-line import/no-default-export -- deprecated usage
export default TableFooter;
