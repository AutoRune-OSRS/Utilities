package io.autorune.utilities.jar

import io.autorune.utilities.asm.Assembly
import io.autorune.utilities.asm.Mask
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.*
import java.nio.file.Path
import java.util.jar.*

class JarLoader {

    private val classNodes = arrayListOf<ClassNode>()
    private var jarRevision: Int = 0

    fun loadJar(location: Path): ArrayList<ClassNode> {
        val jar = JarFile(location.toFile())

        val enumerator = jar.entries()

        while (enumerator.hasMoreElements()) {
            val entry = enumerator.nextElement()
            if (entry.name.endsWith(".class"))
                handleClassEntry(ClassReader(jar.getInputStream(entry)))
        }

        jar.close()

        println("Loaded client jar: $jarRevision")

        return classNodes
    }

    fun getRevision(): Int {
        return jarRevision
    }

    private fun handleClassEntry(classReader: ClassReader) {
        val classNode = ClassNode()
        classReader.accept(classNode, ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
        classNodes.add(classNode)
        if(classNode.name == "client")
        {

            val mn = classNode.methods.first { it.name == "init" }
            val pattern : List<AbstractInsnNode> = Assembly.find(mn,
                    Mask.SIPUSH.operand(765),  //Applet width
                    Mask.SIPUSH.operand(503),  //Applet height
                    //Client version
                    //Dummy parameter
                    Mask.INVOKEVIRTUAL.distance(3) //initializeApplet
            )
            val appHeight = pattern[1] as IntInsnNode
            val clientVersion = appHeight.next
            if(clientVersion is IntInsnNode)
                jarRevision = clientVersion.operand
        }
    }

}