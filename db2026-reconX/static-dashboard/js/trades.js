(function () {
  const table = document.getElementById('trades-table');
  if (!table) return;

  const tbody = table.querySelector('tbody');
  const headers = Array.from(table.querySelectorAll('thead th'));
  const columns = headers.map((header) => header.dataset.col);
  let rows = [];

  function renderRows() {
    tbody.innerHTML = rows.map((row) => {
      const cells = columns.map((col) => `<td>${row[col] ?? ''}</td>`).join('');
      return `<tr>${cells}</tr>`;
    }).join('');
  }

  function sortRows(column, type, direction) {
    rows.sort((left, right) => {
      const leftValue = left[column] ?? '';
      const rightValue = right[column] ?? '';

      if (type === 'number') {
        return (Number(leftValue) - Number(rightValue)) * direction;
      }

      return String(leftValue).localeCompare(String(rightValue), undefined, {
        numeric: true,
        sensitivity: 'base'
      }) * direction;
    });
  }

  headers.forEach((header) => {
    header.addEventListener('click', (event) => {
      if (event.target.classList.contains('resize-handle')) {
        return;
      }

      const column = header.dataset.col;
      const type = header.dataset.type || 'string';
      const currentSort = header.getAttribute('aria-sort');
      const nextDirection = currentSort === 'ascending' ? -1 : 1;

      headers.forEach((th) => {
        th.removeAttribute('aria-sort');
      });

      header.setAttribute('aria-sort', nextDirection === 1 ? 'ascending' : 'descending');
      sortRows(column, type, nextDirection);
      renderRows();
    });
  });

  headers.forEach((header) => {
    const handle = header.querySelector('.resize-handle');
    if (!handle) return;

    handle.addEventListener('mousedown', (event) => {
      event.preventDefault();
      event.stopPropagation();

      const startX = event.clientX;
      const startWidth = header.offsetWidth;

      const onMouseMove = (moveEvent) => {
        header.style.width = (startWidth + moveEvent.clientX - startX) + 'px';
      };

      const onMouseUp = () => {
        document.removeEventListener('mousemove', onMouseMove);
        document.removeEventListener('mouseup', onMouseUp);
      };

      document.addEventListener('mousemove', onMouseMove);
      document.addEventListener('mouseup', onMouseUp);
    });
  });

  fetch('/api/v1/trades?size=200')
    .then((response) => {
      if (!response.ok) {
        throw new Error('Unable to load trades');
      }
      return response.json();
    })
    .then((data) => {
      rows = data.content || data;
      renderRows();
    })
    .catch((error) => {
      console.error('Failed to load trades:', error);
      tbody.innerHTML = '<tr><td colspan="5">Unable to load trades.</td></tr>';
    });
})();
