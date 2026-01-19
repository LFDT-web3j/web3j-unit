/*
 * Copyright 2019 Web3 Labs Ltd.
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
package org.web3j.container.besu

import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.containers.wait.strategy.WaitStrategy
import org.web3j.container.KGenericContainer

class BesuContainer(
    version: String?,
    resourceFiles: HashMap<String, String>,
    hostFiles: HashMap<String, String>,
    genesisPath: String,
    rpcPort: Int,
) :
    KGenericContainer(
        "hyperledger/besu",
        version,
        resourceFiles,
        hostFiles,
        "besu/besu_start.sh",
        if (genesisPath == "dev") "besu/$genesisPath" else genesisPath,
        rpcPort,
    ) {

    override fun withWaitStrategy(): WaitStrategy =
        Wait
            .forHttp("/liveness")
            .forStatusCode(200).forPort(8545)
}
