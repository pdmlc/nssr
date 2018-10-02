cmake_minimum_required(VERSION 3.11)
project(nssr)

add_executable(nssr)
target_compile_features(nssr PUBLIC cxx_std_17)
target_include_directories(nssr
	PUBLIC
		"include"
)
target_sources(nssr
	PUBLIC
		"src/main.cpp"
)
