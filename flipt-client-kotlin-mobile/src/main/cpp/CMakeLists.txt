cmake_minimum_required(VERSION 3.10.2)
project(fliptengine_wrapper VERSION 1.0)

set(CMAKE_BUILD_RPATH "$ORIGIN")

add_library( libfliptengine STATIC IMPORTED )
set_property(TARGET libfliptengine PROPERTY IMPORTED_LOCATION "${CMAKE_CURRENT_SOURCE_DIR}/libs/libfliptengine.a")

add_library(fliptengine_wrapper SHARED fliptengine_wrapper.c)
target_link_libraries(fliptengine_wrapper PRIVATE libfliptengine)

set_target_properties(fliptengine_wrapper PROPERTIES
        VERSION $(PROJECT_VERSION)
        SOVERSION 1
)