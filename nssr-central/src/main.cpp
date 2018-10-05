#include <iostream>
#include <jni.h>

int main(int argc, char **argv) {
	std::cout << "hello" << std::endl;

	JavaVM *jvm;
	JNIEnv *env;

	JavaVMInitArgs vm_args;
	JavaVMOption* options = new JavaVMOption[1];
	options[0].optionString = "-Djava.class.path=C:/Program Files/Java/jdk1.8.0_181";
	vm_args.version = JNI_VERSION_1_6;
	vm_args.nOptions = 1;
	vm_args.options = options;
	vm_args.ignoreUnrecognized = false;

	JNI_CreateJavaVM(&jvm, (void**)&env, &vm_args);
	delete options;

	jclass cls = env->FindClass("Process");
	jmethodID mid = env->GetStaticMethodID(cls, "process", "V");
	env->CallStaticVoidMethod(cls, mid, 100);

	jvm->DestroyJavaVM();

	return 0;
}
