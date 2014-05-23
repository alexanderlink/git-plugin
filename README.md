Expected branch spec behaviour
=====

See also docs/BranchSpecBehaviour.ods (Excel, Open Office,...)

**ls-remote**

In current versions of the git and git-client plugin (state 23. May 2014) we have the following issue. Depending on the branches a remote repository contains CI jobs are triggered again and again. The reason is that `ls-remote` is used in `GitAPI(CliGitAPIImpl).getHeadRev(String, String)` which return a list of matching revisions for a branch spec (pattern). The plugin then simply takes the first entry, although it might be the wrong one. Since the commit ids do not match the job is triggered - this happens again and again.

Assume branches master, feature1/master, feature2/master.
`git ls-remote <repo-url> master` would return all three, sorted alphabetically and therefore feature1/master is taken. But obviously master is what we really meant and what is checked out when the build runs!

To get unique results `refs/heads/branchname` should be used with `ls-remote`.
Therefore my proposal would be to allow "refs/heads/branchname" as branchSpec in the job config to get unique results.

**rev-parse**

During checkout `git rev-parse` is used in `DefaultBuildChooser.getCandidateRevisions(boolean, String, GitClient, TaskListener, BuildData, BuildChooserContext)` to get matching revision(s).
For unique `rev-parse` results `refs/remotes/<remotename>/branchname` should be used in case a specific branch is meant.
Previous plugin versions tried `rev-parse origin/refs/heads/master` and for a branch spec "refs/heads/master" which is not what we want. But this is already fixed and merged.