################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../client/Client.cpp 

OBJS += \
./client/Client.o 

CPP_DEPS += \
./client/Client.d 


# Each subdirectory must supply rules for building sources it contributes
client/%.o: ../client/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -DHAVE_CONFIG_H -I/usr/local/include/thrift -I/Users/ibm/Documents/Development/boost_1_42_0 -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o"$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


