################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
O_SRCS += \
../gen-cpp-old/WxsGatewayService.o \
../gen-cpp-old/WxsGatewayService_server.skeleton.o \
../gen-cpp-old/wxs_constants.o \
../gen-cpp-old/wxs_types.o 

CPP_SRCS += \
../gen-cpp-old/WxsGatewayService.cpp \
../gen-cpp-old/WxsGatewayService_server.skeleton.cpp \
../gen-cpp-old/wxs_constants.cpp \
../gen-cpp-old/wxs_types.cpp 

OBJS += \
./gen-cpp-old/WxsGatewayService.o \
./gen-cpp-old/WxsGatewayService_server.skeleton.o \
./gen-cpp-old/wxs_constants.o \
./gen-cpp-old/wxs_types.o 

CPP_DEPS += \
./gen-cpp-old/WxsGatewayService.d \
./gen-cpp-old/WxsGatewayService_server.skeleton.d \
./gen-cpp-old/wxs_constants.d \
./gen-cpp-old/wxs_types.d 


# Each subdirectory must supply rules for building sources it contributes
gen-cpp-old/%.o: ../gen-cpp-old/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -DHAVE_CONFIG_H -I/usr/local/include/thrift -I/Users/ibm/Documents/Development/boost_1_42_0 -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o"$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


