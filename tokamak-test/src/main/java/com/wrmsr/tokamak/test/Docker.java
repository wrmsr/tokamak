/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wrmsr.tokamak.test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.wrmsr.tokamak.util.Json;
import com.wrmsr.tokamak.util.io.CrLfByteReader;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Docker
{
    private Docker()
    {
    }

    public static SocketAddress socketAddress(String socketName)
            throws IOException
    {
        int colon = socketName.lastIndexOf(':');
        int slashOrBackslash = Math.max(socketName.lastIndexOf('/'), socketName.lastIndexOf('\\'));

        if (socketName.startsWith("@")) {
            // abstract namespace (Linux only!)
            return AFUNIXSocketAddress.inAbstractNamespace(socketName.substring(1));
        }
        else if (colon > 0 && slashOrBackslash < colon && !socketName.startsWith("/")) {
            // assume TCP socket
            String hostname = socketName.substring(0, colon);
            int port = Integer.parseInt(socketName.substring(colon + 1));
            return new InetSocketAddress(hostname, port);
        }
        else {
            // assume unix socket file name
            return new AFUNIXSocketAddress(new File(socketName));
        }
    }

    public static Socket connectSocket(SocketAddress socketAddress)
            throws IOException
    {
        if (socketAddress instanceof AFUNIXSocketAddress) {
            return AFUNIXSocket.connectTo((AFUNIXSocketAddress) socketAddress);
        }
        else {
            Socket socket = new Socket();
            socket.connect(socketAddress);
            return socket;
        }
    }

    //  echo -e 'GET /containers/json HTTP/1.1\r\nHost: v1.40\r\nAccept: */*\r\n' | socat - UNIX-CONNECT:/var/run/docker.sock

    public static final class Container
    {
        public static final class Port
        {
            private final int privatePort;
            private final int publicPort;
            private final String type;

            @JsonCreator
            public Port(
                    @JsonProperty("PrivatePort") int privatePort,
                    @JsonProperty("PublicPort") int publicPort,
                    @JsonProperty("Type") String type)
            {
                this.privatePort = privatePort;
                this.publicPort = publicPort;
                this.type = type;
            }
        }

        private final String id;
        private final Set<String> names;
        private final String image;
        private final String imageId;
        private final List<Port> ports;

        @JsonCreator
        public Container(
                @JsonProperty("Id") String id,
                @JsonProperty("Names") Set<String> names,
                @JsonProperty("Image") String image,
                @JsonProperty("ImageID") String imageId,
                @JsonProperty("Ports") List<Port> ports)
        {
            this.id = id;
            this.names = names;
            this.image = image;
            this.imageId = imageId;
            this.ports = ports;
        }
    }

    public static void queryDocker()
            throws IOException
    {
        Socket sock = connectSocket(socketAddress("/var/run/docker.sock"));
        sock.getOutputStream().write("GET /containers/json HTTP/1.1\r\nHost: v1.40\r\nAccept: */*\r\n\r\n".getBytes(Charsets.UTF_8));
        sock.getOutputStream().close();
        byte[] buf = ByteStreams.toByteArray(sock.getInputStream());

        CrLfByteReader reader = new CrLfByteReader(new ByteArrayInputStream(buf));
        String code = reader.nextLineAscii();

        while (true) {
            byte[] line = reader.nextLine();
            if (line.length == 0) {
                break;
            }
        }

        String body = reader.restUtf8();
        System.out.println(body);

        List<Container> list = Json.readValue(body, new TypeReference<List<Container>>() {});
        System.out.println(list);
    }
}
