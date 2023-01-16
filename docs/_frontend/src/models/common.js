export const SEARCH_ORIGIN = 'http://64.225.77.157:3000';

export const toSearchString = (params) => {
  const searchParams = Object.keys(params).reduce((acc, k) => {
    if (params[k]) {
      switch (k) {
        case '_type':
          break;

        case 'supported':
          acc.append(k, Number(params[k]));
          break;

        case 'tags':
        case 'predicted_entities':
          params[k].forEach((v) => {
            acc.append(k, v);
          });
          break;
        case 'sort':
          if (params[k] !== 'date') {
            acc.append(k, params[k]);
          }
          break;
        case 'recommended':
          break;
        default:
          acc.append(k, params[k]);
          break;
      }
    } else {
      if (k === 'recommended') acc.append(k, Number(params[k]));
    }
    return acc;
  }, new URLSearchParams());

  const search = searchParams.toString();
  return search ? '?' + search : '';
};
