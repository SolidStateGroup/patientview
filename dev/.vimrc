fun! Navigate(target,postfix)
  let s:cf=expand("%:p")
  let s:trg=substitute(s:cf, '\(sql\|test\)', a:target, "")
  let s:nm=substitute(s:trg, '\(_test\.sql\|\.sql\)', a:postfix, '')
  exec "e" . s:nm
  return s:trg
endfun

command! Ss call Navigate("sql", '.sql')
command! St call Navigate("test", '_test.sql')
