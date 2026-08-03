import React, { createContext, useContext, useMemo, useState } from 'react';

const DataTableContext = createContext(null);

function useDataTable() {
  const context = useContext(DataTableContext);

  if (!context) {
    throw new Error('DataTable subcomponents must be used within <DataTable>.');
  }

  return context;
}

export default function DataTable({ data = [], pageSize = 10, children }) {
  const [page, setPage] = useState(0);
  const [sortKey, setSortKey] = useState(null);
  const [sortDir, setSortDir] = useState('asc');

  const sorted = useMemo(() => {
    if (!sortKey) {
      return [...data];
    }

    return [...data].sort((left, right) => {
      const leftValue = left[sortKey];
      const rightValue = right[sortKey];
      const direction = sortDir === 'asc' ? 1 : -1;

      if (leftValue == null && rightValue == null) return 0;
      if (leftValue == null) return 1 * direction;
      if (rightValue == null) return -1 * direction;

      if (typeof leftValue === 'number' && typeof rightValue === 'number') {
        return (leftValue - rightValue) * direction;
      }

      return String(leftValue).localeCompare(String(rightValue), undefined, { numeric: true }) * direction;
    });
  }, [data, sortKey, sortDir]);

  const totalRows = sorted.length;

  const paged = useMemo(() => {
    const start = page * pageSize;
    const end = start + pageSize;
    return sorted.slice(start, end);
  }, [sorted, page, pageSize]);

  const value = useMemo(() => ({
    rows: paged,
    totalRows,
    page,
    pageSize,
    setPage,
    sortKey,
    sortDir,
    setSortKey,
    setSortDir,
  }), [paged, totalRows, page, pageSize, sortKey, sortDir]);

  return (
    <DataTableContext.Provider value={value}>
      <div className="data-table">{children}</div>
    </DataTableContext.Provider>
  );
}

function Header({ columns }) {
  const { sortKey, sortDir, setSortKey, setSortDir } = useDataTable();

  const handleSort = (nextKey) => {
    if (sortKey === nextKey) {
      setSortDir((currentDir) => (currentDir === 'asc' ? 'desc' : 'asc'));
      return;
    }

    setSortKey(nextKey);
    setSortDir('asc');
  };

  return (
    <div className="data-table__header" role="row">
      {columns.map((column) => {
        const isActive = sortKey === column.key;
        const arrow = isActive ? (sortDir === 'asc' ? ' ↑' : ' ↓') : '';

        return (
          <button
            key={column.key}
            type="button"
            className={isActive ? 'is-active' : ''}
            onClick={() => handleSort(column.key)}
          >
            {column.label}{arrow}
          </button>
        );
      })}
    </div>
  );
}

function Body({ renderRow }) {
  const { rows } = useDataTable();

  return (
    <div className="data-table__body">
      {rows.map((row, index) => (
        <div className="data-table__row" key={row.id ?? `${row.tradeRef ?? 'row'}-${index}`}>
          {renderRow(row)}
        </div>
      ))}
    </div>
  );
}

function Pagination() {
  const { page, pageSize, totalRows, setPage } = useDataTable();
  const totalPages = Math.max(1, Math.ceil(totalRows / pageSize));

  return (
    <nav className="data-table__pagination" aria-label="Pagination">
      <button type="button" disabled={page === 0} onClick={() => setPage((current) => Math.max(0, current - 1))}>
        Prev
      </button>
      <span>{page + 1} / {totalPages}</span>
      <button type="button" disabled={page >= totalPages - 1} onClick={() => setPage((current) => Math.min(totalPages - 1, current + 1))}>
        Next
      </button>
    </nav>
  );
}

DataTable.Header = Header;
DataTable.Body = Body;
DataTable.Pagination = Pagination;
