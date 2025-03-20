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
package org.web3j.container

import org.web3j.NodeType
import org.web3j.abi.datatypes.Address
import org.web3j.container.besu.BesuContainer
import org.web3j.container.embedded.EmbeddedService
import org.web3j.container.geth.GethContainer
import org.web3j.evm.Configuration
import org.web3j.evm.PassthroughTracer
import java.net.URI

class ServiceBuilder {

    private lateinit var genesisPath: String
    private lateinit var type: NodeType
    private lateinit var selfAddress: String
    private lateinit var dockerCompose: String
    private var version: String? = null
    private val resourceFiles: HashMap<String, String> = hashMapOf()
    private val hostFiles: HashMap<String, String> = hashMapOf()

    // Default values set to use the docker-compose file
    private var serviceName = "ethrpc1"
    private var servicePort: Int = 8545

    fun type(type: NodeType) = apply {
        this.type = type
    }

    fun version(version: String?) = apply {
        this.version = version
    }

    fun withGenesis(genesisPath: String) = apply {
        this.genesisPath = genesisPath
    }

    fun withDockerCompose(dockerCompose: String) = apply {
        this.dockerCompose = dockerCompose
    }

    fun withSelfAddress(selfAddress: String) = apply {
        this.selfAddress = selfAddress
    }

    fun withServiceName(serviceName: String) = apply {
        this.serviceName = serviceName
    }

    fun withServicePort(servicePort: Int) = apply {
        this.servicePort = servicePort
    }

    fun build(): GenericService {
        return when (type) {
            NodeType.BESU -> BesuContainer(version, resourceFiles, hostFiles, genesisPath, servicePort)
            NodeType.GETH -> GethContainer("v1.13.15", resourceFiles, hostFiles, genesisPath, servicePort)
            NodeType.EMBEDDED -> {
                if (genesisPath == "dev") {
                    EmbeddedService(Configuration(Address(selfAddress), 10), PassthroughTracer())
                } else {
                    EmbeddedService(Configuration(Address(selfAddress), 10, URI(genesisPath).toURL()), PassthroughTracer())
                }
            }
            NodeType.COMPOSE -> KDockerComposeContainer(dockerCompose, serviceName, servicePort)
        }
    }
}
