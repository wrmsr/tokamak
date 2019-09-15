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

package com.wrmsr.tokamak.util.io;

import java.io.IOException;
import java.net.Socket;

import static com.google.common.base.Preconditions.checkNotNull;

public class JdkTcpClient
        implements TcpClient
{
    public class Connection
            implements TcpClient.Connection
    {
        private final Socket socket;

        public Connection(Socket socket)
        {
            this.socket = checkNotNull(socket);
        }

        @Override
        public void send(byte[] data)
        {
            try {
                socket.getOutputStream().write(data);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public int recv(byte[] buf)
        {
            try {
                return socket.getInputStream().read(buf);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void close()
        {
            try {
                socket.close();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public TcpClient.Connection connect(String host, int port)
    {
        try {
            Socket sock = new Socket(host, port);
            return new Connection(sock);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
