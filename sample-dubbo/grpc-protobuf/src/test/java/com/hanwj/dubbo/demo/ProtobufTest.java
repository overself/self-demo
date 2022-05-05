package com.hanwj.dubbo.demo;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hanwj.dubbo.type.StringValue;
import com.hanwj.dubbo.user.Staff;
import com.hanwj.dubbo.user.StaffOrBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class ProtobufTest {

    @Test
    public void TestDemoServiceProto() {
        log.info("TestDemoServiceProto:{}", DemoServiceProto.getDescriptor().toProto().toString());
        Descriptors.FileDescriptor fileDescriptor = DemoServiceProto.getDescriptor();
        log.info("fileDescriptor:{}", fileDescriptor.findServiceByName("DemoService"));

        HelloRequest.Builder builder = HelloRequest.newBuilder();
        builder.setName("你就是个傻子");
        HelloRequest helloRequest = builder.build();
        log.info("HelloRequest:{}",helloRequest.getName());

        try {
            helloRequest = HelloRequest.parseFrom(helloRequest.toByteArray());
            log.info("HelloRequest.parseFrom:{}",helloRequest.getName());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void TestWrapperProto() {
        StringValue.Builder stringB = StringValue.newBuilder();
        stringB.setValue("模拟序列化和反序列化过程");
        //stringB.setValue("1234567890");
        StringValue stringValue = stringB.build();
        log.info("StringValue:{}", stringValue.getValue());
        log.info("ByteArray:{}",stringValue.toByteArray());

        ByteString byteString =  stringValue.getValueBytes();
        log.info("ByteString:{}",byteString.toByteArray());
    }

    @Test
    public void TestPersons() {

        Staff.Builder builder = Staff.newBuilder();
        //2、 设置Person的属性
        builder.setAge(20);
        builder.setName("java的架构师技术栈");

        Staff.PhoneNumber.Builder phone = Staff.PhoneNumber.newBuilder();
        phone.setNumber("13304045538");
        Staff.PhoneType type = Staff.PhoneType.forNumber(1);
        phone.setType(type);
        builder.addPhone(phone.setType(type).build());

        builder.addPhone(Staff.PhoneNumber.newBuilder().setNumber("13591197777").setType(Staff.PhoneType.forNumber(0)).build());
        builder.addPhone(Staff.PhoneNumber.newBuilder().setNumber("04116311653").setType(Staff.PhoneType.forNumber(0)).build());

        Staff.Map.Builder mapBuilder = Staff.Map.newBuilder();
        mapBuilder.setKey("attribute").setValue(999);
        builder.setMap(mapBuilder.build());

        //3、 创建Person
        Staff staff = builder.build();
        //4、序列化
        byte[] data = staff.toByteArray();
        //5、将data保存在本地或者是传到网络

        try {
            //一行代码实现反序列化，data可以是本地数据或者是网络数据
            StaffOrBuilder person = Staff.parseFrom(data);
            log.info("Age:{}", person.getAge());
            log.info("Name:{}", person.getName());
            log.info("Map:{}", person.getMap());
            log.info("PhoneCount:{}", person.getPhoneCount());
            log.info("PhoneList:{}", person.getPhoneList());
            for (Staff.PhoneNumber phoneNumber : person.getPhoneList()) {
                log.info("for PhoneNumber:{}-{}", phoneNumber.getNumber(), phoneNumber.getType());
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

    }

}
