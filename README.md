Expected branch spec behaviour
=====

| # | BranchSpec | Repository State | Expected branch to fetch/checkout | Expected `GIT_BRANCH`
--- | --- | --- | --- | ---
1 | `master` | contains unambiguous `master` branch | `master`<br/>_refs/remotes/origin/master_ | `master`
2 | `abc` | contains unambiguous `abc` branch | `abc`<br/>_refs/remotes/origin/abc_  | `abc` 
3 | `master` | contains branches<br/>`master`,`feature1/master` | `master`<br/>_refs/remotes/origin/master_ | `master`
4 | `feature1/master` | contains branches<br/>`master`,`feature1/master` | `feature1/master`<br/>_refs/remotes/origin/feature1/master_ | `feature1/master`
5 | `origin/master` | remote `origin` contains unambiguous branch `master` | `master` in remote `origin`<br/>_refs/remotes/origin/master_ | `origin/master`
6 | `origin/master` | remote `origin` contains branches<br/>`master` and `origin/master` | `master` in remote `origin`<br/>_refs/remotes/origin/master_ | `origin/master`
7 | `origin/master` | remote is named `xyz` and contains branches<br/>`master` and `origin/master` | branch (!) `origin/master` <br/>_refs/remotes/xyz/origin/master_ | `origin/master` (?)
8 | `origin/master` | two remotes `origin` and `xyz` containing branches<br/>`master` and `origin/master` | ??? | ???
9 | `refs/remotes/origin/master` | contains unambiguous `master` branch | `master` <br/>_refs/remotes/origin/master_ | `master` ???
10 | `refs/heads/master` | contains unambiguous `master` branch | `master` <br/>_refs/remotes/origin/master_ | `master` ???
11| `refs/heads/master` | contains branches `master` _refs/heads/master_, `origin/master` _refs/heads/origin/master_ and `refs/heads/master` _refs/heads/refs/heads/master_ | `master` <br/>_refs/remotes/origin/master_ | `master` ???
