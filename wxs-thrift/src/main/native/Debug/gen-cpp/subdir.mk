################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../gen-cpp/WxsGatewayService.cpp \
../gen-cpp/WxsGatewayService_server.skeleton.cpp \
../gen-cpp/wxs_constants.cpp \
../gen-cpp/wxs_types.cpp 

OBJS += \
./gen-cpp/WxsGatewayService.o \
./gen-cpp/WxsGatewayService_server.skeleton.o \
./gen-cpp/wxs_constants.o \
./gen-cpp/wxs_types.o 

CPP_DEPS += \
./gen-cpp/WxsGatewayService.d \
./gen-cpp/WxsGatewayService_server.skeleton.d \
./gen-cpp/wxs_constants.d \
./gen-cpp/wxs_types.d 


# Each subdirectory must supply rules for building sources it contributes
gen-cpp/%.o: ../gen-cpp/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -DHAVE_CONFIG_H -I/usr/local/include/thrift -I/Users/ibm/Documents/Development/boost_1_42_0 -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o"$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


