package org.codehaus.jremoting.client.transports;

import org.jmock.MockObjectTestCase;

import java.io.IOException;

public class ClientCustomStreamDriverTestCase extends MockObjectTestCase {

    public void testFoo() throws IOException, ClassNotFoundException {

//        ConnectionOpened co = new ConnectionOpened();
//        byte[] bytes = SerializationHelper.getBytesFromInstance(co);
//
//        ByteArrayOutputStream tmp = new ByteArrayOutputStream();
//        DataOutputStream dos = new DataOutputStream(tmp);
//        dos.writeInt(bytes.length);
//        dos.write(bytes);
//        dos.flush();
//
//        ByteArrayInputStream inputStream = new ByteArrayInputStream(tmp.toByteArray());
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//
//        ClientCustomStreamDriver d = new ClientCustomStreamDriver(inputStream, outputStream, this.getClass().getClassLoader());
//
//        Response resp = d.postRequest(new OpenConnection());
//
//        assertNotNull(resp);
//        assertTrue(resp instanceof ConnectionOpened);
//        assertNotSame(resp, co);
//
//        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(outputStream.toByteArray()));
//        int size = dis.readInt();
//        //byte[] bytes2 = dis.read();

    }

}