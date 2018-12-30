(ns dashdb.query.regex)

(def create_re #"(?i)CREATE(\s*\(\s*([A-Za-z]{1,}:?){1,}(\s*,*\s*|\s*)(\s{1,}\{\s*[A-Za-z]{1,}\s*:\s*([A-Za-z]{1,}|[0-9]{1,})((\s*,\s*|\s{1,})[A-Za-z]{1,}\s*:\s*([A-Za-z]{1,}|[0-9]{1,}))*\s*\})?\s*\)\s*\,?){1,}")
