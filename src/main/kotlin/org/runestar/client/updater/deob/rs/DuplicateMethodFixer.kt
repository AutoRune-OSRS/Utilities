package org.runestar.client.updater.deob.rs

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.kxtra.slf4j.getLogger
import org.kxtra.slf4j.info
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.runestar.client.updater.deob.Transformer
import org.runestar.client.updater.deob.util.readJar
import org.runestar.client.updater.deob.util.writeJar
import java.lang.reflect.Modifier
import java.nio.file.Path

object DuplicateMethodFixer : Transformer {

    private val mapper = jacksonObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)

    private val logger = getLogger()

    override fun transform(source: Path, destination: Path) {

        val classNodes = readJar(source)

        var map = HashMap<String, MutableList<MethodNode>>()

        classNodes.forEach { c ->
            c.methods.filter { it.name != "<clinit>" && it.name != "<init>" }.forEach { m ->
                val key = c.name + "." + m.name + m.desc
                if (!map.containsKey(key))
                    map[key] = mutableListOf()
                map[key]?.add(m)
            }
        }

        map = map.filter { it.value.size > 1 } as HashMap<String, MutableList<MethodNode>>

        logger.info { "Duplicate methods fixed: ${map.size}" }

        map.entries.forEach {

            dup ->

            val value = dup.key
            val className = value.split(".")[0]
            val methodName = value.split(".")[1].split("(")[0]

            val owningClassNode = classNodes.first { it.name == className }
            val problemMethodNode = dup.value.first { it.name == methodName && Modifier.isStatic(it.access) }

            val newMethodName = methodName+"_new"

            problemMethodNode.name = newMethodName

            for (classNode in classNodes) {

                for (methodNode in classNode.methods) {

                    methodNode.instructions

                    for (instr in methodNode.instructions) {

                        if (instr !is MethodInsnNode || instr.opcode != Opcodes.INVOKESTATIC)
                            continue

                        val minsn = instr

                        if ((minsn.owner + "." + minsn.name + minsn.desc) != value)
                            continue

                        minsn.name = newMethodName

                    }

                }

            }

        }

        writeJar(classNodes, destination)

    }

}