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
package org.web3j

import java.nio.file.Path
import java.util.Optional
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ConditionEvaluationResult
import org.junit.jupiter.api.extension.ExecutionCondition
import org.junit.jupiter.api.extension.ExtensionConfigurationException
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import org.junit.jupiter.api.io.TempDir
import org.junit.platform.commons.util.AnnotationUtils
import org.web3j.container.ContainerBuilder
import org.web3j.container.KGenericContainer
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.tx.FastRawTransactionManager
import org.web3j.tx.TransactionManager
import org.web3j.tx.gas.ContractGasProvider
import org.web3j.tx.gas.DefaultGasProvider
import org.web3j.tx.response.PollingTransactionReceiptProcessor
import org.web3j.utils.Async

class EVMExtension : ExecutionCondition, BeforeAllCallback, AfterAllCallback, ParameterResolver {

    @TempDir lateinit var tempDir: Path

    val credentials = Credentials
        .create("0x8f2a55949038a9610f50fb23b5883af3b4ecb3c3bb792cbcefbd1542c692be63")

    val gasProvider = DefaultGasProvider()

    lateinit var container: KGenericContainer

    lateinit var web3j: Web3j

    lateinit var transactionManager: TransactionManager

    override fun evaluateExecutionCondition(context: ExtensionContext): ConditionEvaluationResult {
        return findEvmTests(context)
            .map { ConditionEvaluationResult.enabled("EVMTest enabled") }
            .orElseThrow { ExtensionConfigurationException("@EVMTest not found") }
    }

    override fun beforeAll(context: ExtensionContext) {
        val evmTest = AnnotationUtils
            .findAnnotation(context.requiredTestClass, EVMTest::class.java).orElseThrow()

        container = ContainerBuilder()
            .type(evmTest.type)
            .version(evmTest.version)
            .withGenesis(evmTest.genesis)
            .build()

        container.startNode()

        web3j = Web3j.build(
            HttpService(
                "http://localhost:" + container.rpcPort
            ), 500, Async.defaultExecutorService()
        )

        transactionManager = FastRawTransactionManager(
            web3j,
            credentials,
            PollingTransactionReceiptProcessor(
                web3j,
                1000,
                30))
    }

    override fun afterAll(context: ExtensionContext) {
        container.stop()
        web3j.shutdown()
    }

    override fun supportsParameter(
        parameterContext: ParameterContext,
        extensionContext: ExtensionContext
    ): Boolean {
        return parameterContext.parameter.type == Web3j::class.java ||
                parameterContext.parameter.type == TransactionManager::class.java ||
                parameterContext.parameter.type == ContractGasProvider::class.java
    }

    override fun resolveParameter(
        parameterContext: ParameterContext,
        extensionContext: ExtensionContext
    ): Any {
        return when {
            parameterContext.parameter.type == Web3j::class.java -> web3j
            parameterContext.parameter.type == TransactionManager::class.java -> transactionManager
            parameterContext.parameter.type == ContractGasProvider::class.java -> gasProvider
            else -> Any()
        }
    }

    private fun findEvmTests(context: ExtensionContext): Optional<EVMTest> {
        var current = Optional.of(context)
        while (current.isPresent) {
            val evmTest = AnnotationUtils
                .findAnnotation(current.get().requiredTestClass, EVMTest::class.java)
            if (evmTest.isPresent) {
                return evmTest
            }
            current = current.get().parent
        }
        return Optional.empty()
    }
}
