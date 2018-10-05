cmake_minimum_required(VERSION 3.11)
project(nssr)

find_package(Java REQUIRED)
find_package(JNI REQUIRED)

add_executable(nssr)
target_compile_features(nssr PUBLIC cxx_std_17)
target_include_directories(nssr
	PUBLIC
		"include"
)
target_sources(nssr
	PUBLIC
		"src/main.cpp"
		"src/process.cpp"
)
include_directories(${JNI_INCLUDE_DIRS})
link_directories(${JNI_LIBRARIES})
message(${JNI_LIBRARIES})