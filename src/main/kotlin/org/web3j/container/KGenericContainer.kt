/*
 * Copyright 2019 web3j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.web3j.container

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.containers.wait.strategy.WaitStrategy
import org.testcontainers.utility.MountableFile

// https://github.com/testcontainers/testcontainers-java/issues/318
open class KGenericContainer(
    imageName: String,
    version: String?,
    private val resourceFiles: HashMap<String, String>,
    private val hostFiles: HashMap<String, String>,
    private val startUpScript: String,
    private val genesis: String
) :
    GenericContainer<KGenericContainer>(imageName + (version?.let { ":$it" } ?: "")) {

    var rpcPort: Int = 0

    fun startNode() {
        resolveGenesis()
        withLogConsumer { println(it.utf8String) }
        withExposedPorts(8545)
        withCopyFileToContainer(MountableFile.forClasspathResource(startUpScript), "/start.sh")
        resourceFiles.forEach { (source, target) ->
            withCopyFileToContainer(MountableFile.forClasspathResource(source), target)
        }
        hostFiles.forEach { (source, target) ->
            withCopyFileToContainer(MountableFile.forHostPath(source), target)
        }
        withCreateContainerCmdModifier { c -> c.withEntrypoint("/start.sh") }
        waitingFor(withWaitStrategy())
        start()
        rpcPort = getMappedPort(8545)
    }

    open fun resolveGenesis() {
        genesis.let {
            val resolvedGenesis = if (it.endsWith(".json")) it else "$it.json"
            if (inClassPath(resolvedGenesis)) {
                resourceFiles[resolvedGenesis] = "/genesis.json"
            } else {
                hostFiles[resolvedGenesis] = "/genesis.json"
            }
        }
    }

    private fun inClassPath(path: String) = this.javaClass.classLoader.getResource(path) != null

    protected open fun withWaitStrategy(): WaitStrategy =
        Wait.forHttp("/").forStatusCode(200).forPort(8545)
}
