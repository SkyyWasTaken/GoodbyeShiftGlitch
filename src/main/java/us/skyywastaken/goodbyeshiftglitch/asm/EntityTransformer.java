package us.skyywastaken.goodbyeshiftglitch.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.ListIterator;

public class EntityTransformer implements IClassTransformer {
    private boolean done = false;
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if(done) {
            return basicClass;
        }
        if(name.equals("net.minecraft.entity.Entity")) {
            return transformClass(ASMUtils.getClassNode(basicClass), getDeobfuscatedMappings());
        } else if(name.equals("pk")) {
            return transformClass(ASMUtils.getClassNode(basicClass), getObfuscatedMappings());
        }
        return basicClass;
    }

    private byte[] transformClass(ClassNode classToTransform, HashMap<String, String> mappings) {
        MethodNode thing = null;
        for(MethodNode currentMethod : classToTransform.methods) {
            if(currentMethod.name.equals(mappings.get("moveEntityMethod")) && currentMethod.desc.equals("(DDD)V")) {
                thing = currentMethod;
            }
        }
        if (thing == null) {
            return ASMUtils.getByteArrayFromClassNode(classToTransform);
        }
        InsnList instructions = thing.instructions;
        ListIterator<AbstractInsnNode> it = instructions.iterator();
        int insertionIndex = 0;
        boolean found = false;
        while(true) {
            AbstractInsnNode currentInsn = null;
            if(it.hasNext()) {
                currentInsn = it.next();
            } else {
                break;
            }

            if(!found && currentInsn instanceof FieldInsnNode && ((FieldInsnNode) currentInsn).name.equals(mappings.get("onGroundField")) && ((FieldInsnNode) currentInsn).desc.equals("Z")) {
                System.out.println("Desc: " + ((FieldInsnNode) currentInsn).desc);
                found = true;
                AbstractInsnNode field = currentInsn;
                System.out.println("Field: " + currentInsn.getClass().getName());
                JumpInsnNode jumNode = (JumpInsnNode) it.next();
                jumNode.setOpcode(Opcodes.IFNE);
                insertionIndex = instructions.indexOf(field);
                instructions.remove(field);
            }
        }

        InsnList fixInsns = new InsnList();
        fixInsns.add(new FieldInsnNode(Opcodes.GETFIELD, mappings.get("entityClass"), mappings.get("worldObjField"), "L" + mappings.get("worldClass") +";"));
        fixInsns.add(new VarInsnNode(Opcodes.ALOAD, 0));
        fixInsns.add(new VarInsnNode(Opcodes.ALOAD, 0));
        fixInsns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, mappings.get("entityClass"), mappings.get("getEntityBoundingBoxMethod"), "()L" + mappings.get("axisAlignedBBClass") + ";", false));
        fixInsns.add(new InsnNode(Opcodes.DCONST_0));
        fixInsns.add(new LdcInsnNode(-1.0D));
        fixInsns.add(new InsnNode(Opcodes.DCONST_0));
        fixInsns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, mappings.get("axisAlignedBBClass"), mappings.get("offsetMethod"), "(DDD)L" + mappings.get("axisAlignedBBClass") + ";", false));
        fixInsns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, mappings.get("worldClass"), mappings.get("getCollidingBoundingBoxesMethod"), "(L" + mappings.get("entityClass") + ";L" + mappings.get("axisAlignedBBClass") + ";)Ljava/util/List;", false));
        fixInsns.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/List", "isEmpty", "()Z", true));
        instructions.insertBefore(instructions.get(insertionIndex), fixInsns);

        thing.instructions = instructions;
        byte[] things = ASMUtils.getByteArrayFromClassNode(classToTransform);
        File outputFile = new File(System.getProperty("user.dir") + "/Entity");
        System.out.println(System.getProperty("user.dir"));
        try {
            FileOutputStream os = new FileOutputStream(outputFile);
            CheckClassAdapter.verify(new ClassReader(things), false, new PrintWriter(os));
            os.close();
            FileOutputStream os2 = new FileOutputStream(new File(System.getProperty("user.dir") + "/Entity.class"));
            os2.write(things);
            os2.close();
        } catch (Exception ignored) {

        }
        System.out.println("Transformation complete! asdfjkl");
        done = true;
        return things;
    }


    private HashMap<String, String> getDeobfuscatedMappings() {
        HashMap<String, String> mappings = new HashMap<>();
        mappings.put("moveEntityMethod", "moveEntity");
        mappings.put("onGroundField", "onGround");
        mappings.put("entityClass", "net/minecraft/entity/Entity");
        mappings.put("worldObjField", "worldObj");
        mappings.put("worldClass", "net/minecraft/world/World");
        mappings.put("getEntityBoundingBoxMethod", "getEntityBoundingBox");
        mappings.put("axisAlignedBBClass", "net/minecraft/util/AxisAlignedBB");
        mappings.put("offsetMethod", "offset");
        mappings.put("getCollidingBoundingBoxesMethod", "getCollidingBoundingBoxes");
        return mappings;
    }

    private HashMap<String, String> getObfuscatedMappings() {
        HashMap<String, String> mappings = new HashMap<>();
        mappings.put("moveEntityMethod", "d");
        mappings.put("onGroundField", "C");
        mappings.put("entityClass", "pk");
        mappings.put("worldObjField", "o");
        mappings.put("worldClass", "adm");
        mappings.put("getEntityBoundingBoxMethod", "aR");
        mappings.put("axisAlignedBBClass", "aug");
        mappings.put("offsetMethod", "c");
        mappings.put("getCollidingBoundingBoxesMethod", "a");
        return mappings;
    }

    private void addNewRenderMethod(ClassNode classToTransform, MethodNode newRenderMethod) {
        classToTransform.methods.add(newRenderMethod);
    }
}
