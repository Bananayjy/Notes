# IDEA中Git的使用

##### 1. 为项目创建版本控制

新建一个项目后（这里以maven工程为例），我们可以依次单击 VCS - import into Version Control - Create Git Reposity 来为当前项目创建一个本地仓库，进行版本控制。

创建本地仓库成功后，会在maven工作目录下会创建一个名为`.git`的隐藏文件

`.git` 文件是Git版本控制系统中的隐藏文件夹，位于项目的根目录下。它是Git仓库的核心部分，包含了跟踪、管理和存储项目历史的所有信息。

在 `.git` 文件夹中，您会找到以下重要的文件和子目录：

1. `HEAD`：该文件保存了当前所在的分支或提交的引用。
2. `config`：该文件包含了Git仓库的配置信息，如用户名、邮箱、远程仓库等。
3. `objects`：该目录存储了Git对象，这些对象代表项目历史中的各个版本、提交和文件内容。
4. `refs`：该目录包含了指向不同分支和标签的引用文件，它们指示了每个分支和标签所指向的提交。
5. `hooks`：该目录包含了可自定义的钩子脚本，可以在特定Git操作发生时执行自定义逻辑。
6. `index`：该文件是Git的暂存区（Staging Area）的索引文件，记录了要提交到下一个版本的文件信息。

除了上述文件和目录之外，`.git` 文件夹还可能包含其他与Git版本控制相关的文件和目录，具体取决于项目的使用情况和配置。

请注意，`.git` 文件夹是一个隐藏文件夹，这意味着在大多数操作系统的默认设置下，它不会在文件浏览器中显示。如果您需要查看或编辑 `.git` 文件夹中的内容，请确保您的文件浏览器具备显示隐藏文件和文件夹的功能。



并且当前maven工作目录下的所有文件都会被加入暂存区

![image-20230807101112683](IDEA%E4%B8%ADGit%E7%9A%84%E4%BD%BF%E7%94%A8.assets/image-20230807101112683.png)









##### 其他Git补充知识点：

**（1）git中的`HEAD`和`orgin/HEAD`**

①HEAD：

- `HEAD` 是一个指向当前所在分支或提交的符号引用。它通常指向当前所在分支的最新提交。
- 在 Git 中，`HEAD` 既可以指向一个分支名（如 `master`），也可以直接指向一个具体的提交哈希值（commit hash）。
- 当您切换分支时，Git 会更新 `HEAD` 的指向以反映当前所在的分支。

②orgin/HEAD：

- `origin/HEAD` 是指向远程仓库的默认分支的符号引用。
- 远程仓库通常有一个默认分支，例如 `origin` 是一个远程仓库，`origin/HEAD` 指向 `origin` 的默认分支。
- 它的作用是在克隆远程仓库时提供一个标识，告诉 Git 远程仓库的默认分支是哪个。



总结：

`HEAD` 可以用来表示当前工作目录的状态，也可以被重置到其他分支或提交来进行操作，如切换分支、回退提交等。

`origin/HEAD` 主要用于多人协作开发时，指示默认分支在远程仓库中的位置。

`HEAD` 是指向当前分支或提交的符号引用，而 `origin/HEAD` 是指向远程仓库的默认分支的符号引用。它们在 Git 中扮演着不同的角色，用于表示工作目录状态和远程仓库的默认分支。



**（2）IDEA中的版本切换：git rest**

git中通过git rest命令进行版本切换（版本回退/回滚）

IDEA中提供了Reset Current Branch to Here Soft 作为版本切换的方法，两者本质上是同一个功能

**作用：**用于切换版本（主要用于回滚版本，合并简化多余的提交记录）

**类型：**对于版本切换，有四种模式，分别是soft、mixed、hard、keep；

![image-20230807154640561](IDEA%E4%B8%ADGit%E7%9A%84%E4%BD%BF%E7%94%A8.assets/image-20230807154640561.png)

①soft：切换版本的同时，保留工作区和index暂存区的内容，只让repository中的内容(已经提交的内容)和 reset 目标节点保持一致，因此原节点和reset节点之间的【差异变更集】会放入index暂存区中(Staged files)。所以效果看起来就是工作目录的内容不变，暂存区原有的内容也不变，只是原节点和Reset节点之间的所有差异都会放到暂存区中（之前写的不会改变，你之前暂存过的文件还在暂存）

官方文档解释：soft将指定提交的内容覆盖到本地仓库库。暂存区、工作区不变；

实例：

在版本v1中，除了一个默认的master分支以外，再创建一个BrachTest1分支，这两个分支都指向版本v1。

我们切换到BrachTest1分支，然后在版本V1中的test1中加入注释“123”，并创建一个test2类，加入到git版本控制中

test1：

```java
package com.example;

/**
 * @author banana
 * @create 2023-08-07 9:52
 */
public class test1 {
    //123
}

```

test2：

```java
package com.example;

/**
 * @author banana
 * @create 2023-08-08 9:09
 */
public class test2 {
}

```

结构：

![image-20230808091659921](IDEA%E4%B8%ADGit%E7%9A%84%E4%BD%BF%E7%94%A8.assets/image-20230808091659921.png)

 Version Control 工具窗口（本地更改工具窗口，显示修改文件列表）

![image-20230808092452004](IDEA%E4%B8%ADGit%E7%9A%84%E4%BD%BF%E7%94%A8.assets/image-20230808092452004.png)

我们将这两个修改提交（在IDEA中，通过Commit Changes功能，直接将git中的add和commit合并）

![image-20230808093313551](IDEA%E4%B8%ADGit%E7%9A%84%E4%BD%BF%E7%94%A8.assets/image-20230808093313551.png)

提交后，我们通过log可以看出，此时BranchTest1分支指向版本2，而master分支指向版本1

![image-20230808093350239](IDEA%E4%B8%ADGit%E7%9A%84%E4%BD%BF%E7%94%A8.assets/image-20230808093350239.png)

我们切换到master分支，去看一下版本1的状态

结构

![image-20230808093503398](IDEA%E4%B8%ADGit%E7%9A%84%E4%BD%BF%E7%94%A8.assets/image-20230808093503398.png)



test1：

```java
package com.example;

/**
 * @author banana
 * @create 2023-08-07 9:52
 */
public class test1 {

}

```

Local Changes：

![image-20230808093529654](IDEA%E4%B8%ADGit%E7%9A%84%E4%BD%BF%E7%94%A8.assets/image-20230808093529654.png)

可以看到此时，版本1的状态（master分支指向版本1）是只有test1类，并且test1类中没有内容，而且在Local Changes没有任何的修改记录（已经提交给了版本2，在版本2的本地仓库中存在）

我们再切换到BranchTest1分支，添加一个test3类并在test1中添加注释“456”

结构：

![image-20230808094324551](IDEA%E4%B8%ADGit%E7%9A%84%E4%BD%BF%E7%94%A8.assets/image-20230808094324551.png)

test1：

```java
package com.example;

/**
 * @author banana
 * @create 2023-08-07 9:52
 */
public class test1 {
    //123
    //456
}

```

test3：

```java
package com.example;

/**
 * @author banana
 * @create 2023-08-08 9:40
 */
public class test3 {
}

```

在Log中右击版本v1，选择版本切换

![image-20230808093758906](IDEA%E4%B8%ADGit%E7%9A%84%E4%BD%BF%E7%94%A8.assets/image-20230808093758906.png)

选择模式为Soft

![image-20230808093814618](IDEA%E4%B8%ADGit%E7%9A%84%E4%BD%BF%E7%94%A8.assets/image-20230808093814618.png)

此时版本1中的结构：

![image-20230808094407741](IDEA%E4%B8%ADGit%E7%9A%84%E4%BD%BF%E7%94%A8.assets/image-20230808094407741.png)

修改列表

![image-20230808094400339](IDEA%E4%B8%ADGit%E7%9A%84%E4%BD%BF%E7%94%A8.assets/image-20230808094400339.png)

test1：

```java
package com.example;

/**
 * @author banana
 * @create 2023-08-07 9:52
 */
public class test1 {
    //123
    //456
}

```

test2：

```java
package com.example;

/**
 * @author banana
 * @create 2023-08-08 9:09
 */
public class test2 {
}

```

test3：

```java
package com.example;

/**
 * @author banana
 * @create 2023-08-08 9:40
 */
public class test3 {
}

```

我们可以看到在切换版本的时候，其保存了我们之前位于暂存区或工作区（这里就统一指Local Changes）中的test3和test1，然后将版本2的仓库提交内容和版本1的仓库提交内容一样（即只有一个test1，并且里面没有任何内容），之后将他们的差异（即之前版本1提交为版本2中的提交内容，即test1中的注释和新建的test2类）再次放到暂存区和工作区中（这里就统一指Local Changes）



②mixed（默认）：切换版本的同时，只保留Working Tree工作目录的內容，但会将 Index暂存区 和 Repository 中的內容更改和reset目标节点一致，因此原节点和Reset节点之间的【差异变更集】会放入Working Tree工作目录中。所以效果看起来就是原节点和Reset节点之间的所有差异都会放到工作目录中。（之前写的不会改变，你之前暂存过的文件不会暂存）

官方文档解释：mixed将指定提交的内容覆盖到本地仓库和暂存区。工作区不变【所以叫做mixed 混合的，混合改变两个地方的内容】；

前面的过程和①中的一样，我们直接看切换版本之后的情况，我们在从版本v2前后到版本v1的时候选择mixed模式

结构：

![image-20230808095701170](IDEA%E4%B8%ADGit%E7%9A%84%E4%BD%BF%E7%94%A8.assets/image-20230808095701170.png)

Local Changes：

![image-20230808095346133](IDEA%E4%B8%ADGit%E7%9A%84%E4%BD%BF%E7%94%A8.assets/image-20230808095346133.png)

test1：

```java
package com.example;

/**
 * @author banana
 * @create 2023-08-07 9:52
 */
public class test1 {
    //123
    //456
}
```

test2：

```java
package com.example;

/**
 * @author banana
 * @create 2023-08-08 9:50
 */
public class test2 {
}

```

test3：

```java
package com.example;

/**
 * @author banana
 * @create 2023-08-08 9:51
 */
public class test3 {
}

```

我们看到只有修改的内容被放到了暂存区，之前暂存的新建的文件都不会暂存（没有进行版本控制了），将切换的版本1的本地仓库提交和暂存区的内容（一个test1并且里面没有任何注释）覆盖当前版本2的仓库提交和暂存区的内容，并且两者的差异会放到工作区中，即文件还没有被跟踪即，即未进行版本控制，注释还在工作区中。（因为这里的Local Changes包括了修改内容，所有工作区和暂存区的都显示了）



③hard：切换版本的同时，直接将工作区、 暂存区及提交仓库 都重置成目标节点的內容，所以效果看起来等同于清空暂存区和工作区。（文件恢复到所选提交状态，任何更改都会丢失）

官方解释：hard将指定的提交覆盖到工作区、暂存区、本地仓库；

前面的步骤和前面一样，在切换版本的时候选择hard模式

结构：

![image-20230808101232390](IDEA%E4%B8%ADGit%E7%9A%84%E4%BD%BF%E7%94%A8.assets/image-20230808101232390.png)

test1：

```java
package com.example;

/**
 * @author banana
 * @create 2023-08-07 9:52
 */
public class test1 {

}

```

Local Changes：

![image-20230808101256980](IDEA%E4%B8%ADGit%E7%9A%84%E4%BD%BF%E7%94%A8.assets/image-20230808101256980-16914607774501.png)

可以看到版本1的工作区、暂存区、本地仓库覆盖了当前版本。



④keep：切换版本的同时，直接将 工作目录 和 提交到本地仓库 都重置成目标目标节点的內容，暂存区 的内容保持不变。(任何本地更改都将丢失，文件将恢复到所选提交的状态，但本地更改将保持不变)

前面的步骤和前面一样，在切换版本的时候选择keep模式

出现提示，因为此时版本1的test1内容是空，当前版本的test1内容有“123”注释和“456”注释，由于暂存区内容保存不变，也就是test1中要有内容“456”，并且工作区要重置和版本1一样，test1是空的，发生了冲突，需要解决冲突

![image-20230808101541794](IDEA%E4%B8%ADGit%E7%9A%84%E4%BD%BF%E7%94%A8.assets/image-20230808101541794.png)


![image-20230808101627295](IDEA%E4%B8%ADGit%E7%9A%84%E4%BD%BF%E7%94%A8.assets/image-20230808101627295.png)

如何选择accept Their，之前暂存区内容不变，冲突的test1为版本1的暂存区（即没有）



![image-20230808101748278](IDEA%E4%B8%ADGit%E7%9A%84%E4%BD%BF%E7%94%A8.assets/image-20230808101748278.png)

![image-20230808101755666](IDEA%E4%B8%ADGit%E7%9A%84%E4%BD%BF%E7%94%A8.assets/image-20230808101755666.png)

如何选择accept yours，之前暂存区内容不变，冲突的test1为当前的暂存区

![image-20230808102346901](IDEA%E4%B8%ADGit%E7%9A%84%E4%BD%BF%E7%94%A8.assets/image-20230808102346901.png)

test1:

```java
package com.example;

/**
 * @author banana
 * @create 2023-08-07 9:52
 */
public class test1 {
    //123
    //456
}

```





**（3）远端仓库和本地仓库的push和pull**

情况一：如果push和pull的分支是独立的（即本地仓库有A分支，远端仓库没有A分支；或本地仓库没有B分支，远端仓库有B分支），那么push和pull操作是不会对分支有影响的。

情况二：如果push和pull的分支是远端仓库和本地仓库共用的，那么就需要对两条分支进行合并，并解决其中的冲突部分（如果是push：先将远端仓库对应的分支pull到本地，与本地的分支进行合并，然后解决其中的冲突部分，再push到远端仓库）



**（4）静态搁置**

同一个项目中的不同分支是共用暂存区的，如果当前在修改A分支，突然要求去修改B分支，如果我们在直接切换的话，会将A分支的修改能容也添加到B分支上，这时候就可以用到静态搁置。



**（5）IDEA的Default Changelist（默认更改列表）**

在 IntelliJ IDEA 中，Default Changelist（默认更改列表）并不是与 Git 的暂存区（Staging Area）直接对应的概念。

Default Changelist 是 IntelliJ IDEA 特有的一个功能，用于跟踪在一个更改集中未提交的修改。它可以包含你在项目中所做的所有修改，无论这些修改是否已经被添加到 Git 的暂存区。

当你在 IntelliJ IDEA 中进行代码修改时，这些更改会自动添加到 Default Changelist 中。你可以使用 Version Control 工具窗口中的 "Local Changes" 选项卡来查看和管理 Default Changelist 中的修改。

注意，虽然 Default Changelist 中的修改可以映射到 Git 的暂存区，并通过 Git 进行提交，但它们并不是完全同步的。你仍然需要手动将 Default Changelist 中的修改添加到 Git 的暂存区才能进行提交。

因此，在 IntelliJ IDEA 中，默认情况下 Default Changelist 并不直接对应 Git 的暂存区，而是一种用于管理待提交修改的工作区域。你可以根据需要将 Default Changelist 中的修改添加到 Git 的暂存区，然后再进行提交操作。



**（6）默认更改列表不用进行文件的提交原因**

当你对文件进行修改后，直接执行 commit 操作而不需要手动将文件添加到暂存区（Git 的 Staging Area）。这是因为 IntelliJ IDEA 默认启用了一个名为 "Commit Changes" 的功能。

"Commit Changes" 功能会自动将你的修改添加到 Git 的暂存区，并在提交时一并提交这些更改。这样可以简化你的工作流程，无需显式执行 add 操作来跟踪和提交文件的更改。

"Commit Changes" 是 IntelliJ IDEA 的默认行为，它会自动将文件更改添加到暂存区，并在提交时一并提交这些更改。因此，默认情况下，你无法直接禁用该功能。



**（7）git中的工作树**

Git 中的 "工作树"（working tree）是指你当前正在进行编辑、修改和开发的项目目录。它包含了项目的实际文件和目录，即你对项目所做的更改。

在 Git 中，工作树与版本库（repository）以及暂存区（staging area）共同构成了三个核心概念：

1. 工作树：也称为 "工作目录" 或 "工作区"，是你当前正在操作和编辑的项目的实际文件和目录。所有未被 Git 跟踪或暂存的更改都发生在工作树中。
2. 版本库：也称为 "仓库" 或 "存储库"，是 Git 用于存储项目的完整历史记录和元数据的地方。它包含了项目的所有分支、提交记录和标签。版本库通常位于项目根目录下的 `.git` 目录中。
3. 暂存区：也称为 "索引" 或 "缓存区"，是 Git 用来暂时存放将要提交的更改的地方。当你执行 `git add` 命令时，将会将工作树中的更改添加到暂存区中。

工作树允许你进行对项目文件的修改、编码和开发工作。你可以通过编辑文件、创建新文件、删除文件等来进行更改，然后使用 Git 将这些更改保存为提交。

当你进行提交时，Git 会将暂存区中的内容保存为一个新的提交，并将工作树中的更改与之关联。这样，你的更改就被永久记录在版本库中，可以随时回溯和恢复。

理解 Git 中的工作树、版本库和暂存区的概念对于有效使用 Git 进行版本控制和协作是至关重要的。



**（8）Detached head（分离的Head）**

"Detached head"（分离的 HEAD）是一个 Git 中的术语，指的是当前所在的工作树（working tree）处于一个无法直接与分支关联的提交状态。这种情况下，Git 的 HEAD 引用不再指向任何分支，而是直接指向一个具体的提交。

当出现 "detached head" 状态时，你处于一个特殊的状态，不再位于任何分支上。这通常发生在以下几种情况下：

1. 切换到一个具体的提交：通过使用 Git 命令（如 `git checkout <commit>`），你可以直接切换到一个具体的提交。在这种情况下，HEAD 将指向这个提交，而不是分支。
2. 强制性更新分支：使用 Git 命令（如 `git reset` 或 `git rebase`）强制移动一个分支的引用，可能会导致 HEAD 处于 "detached head" 状态。
3. 分支合并失败：如果在进行分支合并操作时遇到冲突或其他问题，Git 可能会将 HEAD 状态设置为 "detached head"，以便你解决冲突或处理问题。

在 "detached head" 状态下，你仍然可以查看、修改和提交更改，但这些更改将不会与任何分支相关联。这意味着如果你执行新的提交，没有分支引用指向它们，这些提交可能会在一段时间后被 Git 的垃圾回收机制清理掉。

如果你意外进入了 "detached head" 状态，你可以通过以下方法解决或恢复：

1. 创建新分支：你可以基于当前的提交创建一个新的分支，这样就可以将它们关联起来。使用 `git branch <new-branch-name>` 命令创建一个新分支，并切换到该分支。这样就可以继续在新分支上进行工作。
2. 重置分支：如果你只是希望撤销之前的操作并回到某个特定的分支，可以使用 `git checkout <branch-name>` 命令切换回该分支，Git 将会移动 HEAD 到该分支上。

"Detached head" 状态通常发生在特定场景下，但在正常的开发工作中应尽量避免，以确保更好的版本控制和协作。



**（9）Cherry-Pick**

在 Git 中，"Cherry-pick" 是一种选择性地将一个或多个提交（commits）应用到当前分支的操作。它允许你从其他分支中选择一个或多个提交，并将它们应用到当前分支上，而不是简单地合并整个分支。

Cherry-pick 操作非常有用，特别是在以下几种情况下：

1. 提取特定提交：当你只希望将某个特定的提交应用到当前分支，而不是整个分支的更改时，可以使用 cherry-pick。
2. 合并独立的提交：如果你需要将其他分支中的一些独立的提交合并到当前分支，而不是整个分支的更改，cherry-pick 可以很方便地实现这个目标。

要执行 cherry-pick 操作，请按照以下步骤进行操作：

1. 确保你位于要应用提交的目标分支上。可以使用 `git checkout <branch-name>` 命令切换到目标分支。
2. 找到要应用的提交的哈希值（commit hash）。可以使用 `git log` 命令查看提交历史记录，找到要 cherry-pick 的提交的哈希值。
3. 运行 `git cherry-pick <commit>` 命令，其中 `<commit>` 为要应用的提交的哈希值。这将把指定的提交应用到当前分支上，并创建一个新的提交，包含原始提交的更改。

请注意，cherry-pick 操作会在当前分支上创建一系列新的提交，而这些提交与原始分支的历史记录可能不同。这是因为 cherry-pick 将选定的提交重新应用到不同的分支上，可能会导致冲突或需要手动解决的问题。在执行 cherry-pick 操作之前，最好确保你了解其潜在影响，并进行相应的测试和确认。

此外，Git 也提供了其他相关的命令和选项来帮助你管理和处理提交，例如 `git cherry-pick --continue`（继续应用 cherry-pick）、`git cherry-pick --abort`（中止 cherry-pick）等。可以使用 `git cherry-pick --help` 命令获取更多关于 cherry-pick 的详细信息和用法示例。



实例：

在BranchTest1的版本v1中只有一个test1类，然后创建一个test2类并提交，作为版本v2

![image-20230808104136189](IDEA%E4%B8%ADGit%E7%9A%84%E4%BD%BF%E7%94%A8.assets/image-20230808104136189.png)

![image-20230808104142958](IDEA%E4%B8%ADGit%E7%9A%84%E4%BD%BF%E7%94%A8.assets/image-20230808104142958.png)

此时我们切换到master分支，我们也想添加test2文件，那么我们可以选中版本v2，进行Cherry-pick，将其提交的test2应用到当前分支上

![image-20230808104239340](IDEA%E4%B8%ADGit%E7%9A%84%E4%BD%BF%E7%94%A8.assets/image-20230808104239340.png)

![image-20230808104248960](IDEA%E4%B8%ADGit%E7%9A%84%E4%BD%BF%E7%94%A8.assets/image-20230808104248960.png)

![image-20230808104256492](IDEA%E4%B8%ADGit%E7%9A%84%E4%BD%BF%E7%94%A8.assets/image-20230808104256492.png)