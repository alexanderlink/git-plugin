Expected branch spec behaviour
=====

See also [**docs/BranchSpecBehaviour.ods**](https://github.com/alexanderlink/git-plugin/raw/branchSpecDiscussion/docs/BranchSpecBehaviour.ods) (Excel, Open Office,...)

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

# UPDATE

* git-client-plugin [Pull Request 135](https://github.com/jenkinsci/git-client-plugin/pull/135) was merged - version will be 1.10.0.
* I tested the git-plugin with the latest git-client-plugin (1044437145132de9)
    * The new tests for this issue (see git-plugin [Pull Request 232](https://github.com/jenkinsci/git-plugin/pull/232)) run through successfully
	* Manual tests show that "refs/heads/" branchSpecs work correctly now

However in [**docs/BranchSpecBehaviour.ods**](https://github.com/alexanderlink/git-plugin/raw/branchSpecDiscussion/docs/BranchSpecBehaviour.ods) you can see that there are still issues!
E.g. `LegacyCompatibleGitAPIImpl.extractBranchNameFromBranchSpec(String branchSpec)` still only uses the last segment (e.g. "master" for "feature2/master") if the branchSpec does not start with "refs/heads/", "refs/remotes/", "remotes/" or "refs/tags/". This means we only covered the most urgent scenarios, but for many other scenarios the plugin will behave wrong. See [**docs/BranchSpecBehaviour.ods**](https://github.com/alexanderlink/git-plugin/raw/branchSpecDiscussion/docs/BranchSpecBehaviour.ods) for more details.

To reproduce the issues:
* Clone https://github.com/alexanderlink/git-plugin-test-repo.git and "gitk" to have the branch hierarchy at your fingertips
* Configure a Jenkins Job to fetch from https://github.com/alexanderlink/git-plugin-test-repo.git, Poll every minute ("* * * * *") and build the following branch specs (test one by one):
    * "master"
	    * Build is triggered again and again since `git ls-remote -h https://github.com/alexanderlink/git-plugin-test-repo.git master` is used to identify the commitId on the server. Since the result is ambiguous and the first entry is taken which is the wrong one, the build is triggered again and again every minute because the commitId does not match the old checked out commitId ("new change").
	* "feature3/master"
	    * Although "feature3/master" would be unambiguous (in our test repo - not necessarily in all scenarios!) `LegacyCompatibleGitAPIImpl.extractBranchNameFromBranchSpec(String branchSpec)` still uses only the last segment ("master"). For "master" the same issue applies as in the example above.
    * "feature2/master"
        * works by only by chance because this branch is the first (alphabetic) entry returned by `git ls-remote`.
    * "origin/master"
	    * This branch spec is ambiguous in our scenario ("refs/heads/master" and "refs/heads/origin/master" - a branch called "origin/master"). In this case the build is triggered immediately again and again because "Multiple candidate revisions" are recognized. Unfortunately this mechanism does not work properly in this case which results in endlessly retriggered builds.
	* "mytag"
	    * The job is triggered again and again because ls-remote does not return any result for a tag name. Using "refs/tags/mytag" solves this issue. In this case `ls-remote` is used without `-h` and with the exact spec.
	* "1c75f0fa904d014d6871406a9e32468520fab799"
	    * CommitIds basically work, but if polling is enabled (which does not really make sense) the job is triggered again and again. The reason is that also `git ls-remote -h https://github.com/alexanderlink/git-plugin-test-repo.git 1c75f0fa904d014d6871406a9e32468520fab799` is used, which is useless
	