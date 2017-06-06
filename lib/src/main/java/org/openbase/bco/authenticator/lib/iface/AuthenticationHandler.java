/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openbase.bco.authenticator.lib.iface;

/*-
 * #%L
 * BCO Authentication Library
 * %%
 * Copyright (C) 2017 openbase.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import com.google.protobuf.ByteString;
import java.io.Serializable;
import java.util.List;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.RejectedException;
import rst.domotic.authentification.AuthenticatorTicketType.AuthenticatorTicket;
import rst.domotic.authentification.LoginResponseType.LoginResponse;

/**
 *
 * @author sebastian
 */
public interface AuthenticationHandler {
    
    /**
     * Encrypts any Object into a ByteString
     * @param obj Object to be encrypted
     * @param key byte[] to encrypt obj with
     * @return Returns encrypted object as ByteString
     * @TODO: Exception description
     * NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException, IllegalBlockSizeException
     */
    public ByteString encryptObject(Serializable obj, byte[] key) throws RejectedException;
    
    /**
     * Decrypts a ByteString into an Object
     * @param bstr ByteString to be decrypted
     * @param key  byte[] to decrypt bstr with
     * @return Returns decrypted object as Object
     * @TODO: Exception description
     * IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, ClassNotFoundException, IllegalBlockSizeException, BadPaddingException
     */
    public Object decryptObject(ByteString bstr, byte[] key) throws RejectedException;
    
    /**
     * Hashes client's password symmetrically to server's approach to hash client's password
     * @param clientPassword Client's password
     * @return Returns a byte[] representing the hashed password
     * @TODO: Exception description
     */
    public byte[] initKDCRequest(String clientPassword) throws RejectedException;
            
    /**
     * Handles a Key Distribution Center (KDC) login request
     * Creates a Ticket Granting Server (TGS) session key that is encrypted by the client's password
     * Creates a Ticket Granting Ticket (TGT) that is encrypted by TGS private key
     * @param clientID Identifier of the client - must be present in client database
     * @param clientNetworkAddress Network address of client
     * @param TGSSessionKey TGS session key generated by controller
     * @param TGSPrivateKey TGS private key generated by controller or saved somewhere in the system
     * @return Returns wrapper class containing both the TGT and TGS session key
     * @throws NotAvailableException Throws, if clientID was not found in database
     * @TODO: Exception description
     */
    public LoginResponse handleKDCRequest(String clientID, String clientNetworkAddress, byte[] TGSSessionKey, byte[] TGSPrivateKey) throws NotAvailableException, RejectedException;
    
    /**
     * Handles a KDC response
     * Decrypts the TGS session key with client's hashed password
     * Creates an Authenticator containing the clientID and current timestamp encrypted with the TGS session key
     * @param clientID Identifier of the client - must be present in client database
     * @param hashedClientPassword Client's hashed password
     * @param wrapper TicketSessionKeyWrapper containing the TGT and TGS session key
     * @return Returns a list of objects containing: 
     *         1. An AuthenticatorTicketWrapper containing both the TGT and Authenticator
     *         2. A SessionKey representing the TGS session key
     * @TODO: Exception description
     */
    public List<Object> handleKDCResponse(String clientID, byte[] hashedClientPassword, LoginResponse wrapper) throws RejectedException; 

    /**
     * Handles a Ticket Granting Service (TGS) request
     * Creates a Service Server (SS) session key that is encrypted with the TGS session key
     * Creates a Client Server Ticket (CST) that is encrypted by SS private key
     * @param TGSSessionKey TGS session key generated by controller
     * @param TGSPrivateKey TGS private key generated by controller or saved somewhere in the system
     * @param SSSessionKey SS session key generated by the controller
     * @param SSPrivateKey TGS private key generated by controller or saved somewhere in the system
     * @param wrapper AuthenticatorTicketWrapper that contains both encrypted Authenticator and TGT
     * @return Returns a wrapper class containing both the CST and SS session key
     * @throws RejectedException Throws, if timestamp in Authenticator does not fit to time period in TGT
     *                               or, if clientID in Authenticator does not match clientID in TGT
     * @TODO: Exception description
     */
    public LoginResponse handleTGSRequest(byte[] TGSSessionKey, byte[] TGSPrivateKey, byte[] SSSessionKey, byte[] SSPrivateKey, AuthenticatorTicket wrapper) throws RejectedException;
    
    /**
     * Handles a TGS response
     * Decrypts the SS session key with TGS session key
     * Creates an Authenticator containing the clientID and empty timestamp encrypted with the SS session key
     * @param clientID Identifier of the client - must be present in client database
     * @param TGSSessionKey TGS session key provided by handleKDCResponse()
     * @param wrapper TicketSessionKeyWrapper containing the CST and SS session key
     * @return Returns a list of objects containing: 
     *         1. An AuthenticatorTicketWrapper containing both the CST and Authenticator
     *         2. A SessionKey representing the SS session key
     * @TODO: Exception description
     */
    public List<Object> handleTGSResponse(String clientID, byte[] TGSSessionKey, LoginResponse wrapper) throws RejectedException;
    
    /**
     * Initializes a SS request
     * Sets current timestamp in Authenticator
     * @param SSSessionKey SS session key provided by handleTGSResponse()
     * @param wrapper AuthenticatorTicket wrapper that contains both encrypted Authenticator and CST
     * @return Returns a wrapper class containing both the CST and modified Authenticator
     * @TODO: Exception description
     */
    public AuthenticatorTicket initSSRequest(byte[] SSSessionKey, AuthenticatorTicket wrapper) throws RejectedException;
    
    /**
     * Handles a service method (Remote) request to Service Server (SS) (Manager)
     * Updates given CST's validity period and encrypt again by SS private key
     * @param SSSessionKey SS session key generated by server
     * @param SSPrivateKey SS private key only known to SS
     * @param wrapper AuthenticatorTicket wrapper that contains both encrypted Authenticator and TGT
     * @return Returns a wrapper class containing both the modified CST and unchanged Authenticator
     * @throws RejectedException Throws, if timestamp in Authenticator does not fit to time period in TGT
     *                               or, if clientID in Authenticator does not match clientID in TGT
     * @TODO: Exception description
     */
    public AuthenticatorTicket handleSSRequest(byte[] SSSessionKey, byte[] SSPrivateKey, AuthenticatorTicket wrapper) throws RejectedException;
    
    /**
     * Handles a SS response
     * Decrypts Authenticator of both last- and currentWrapper with SSSessionKey
     * Compares timestamps of both Authenticators with each other
     * @param SSSessionKey SS session key provided by handleTGSResponse()
     * @param lastWrapper Last TicketAuthenticatorWrapper provided by either handleTGSResponse() or handleSSResponse()
     * @param currentWrapper Current TicketAuthenticatorWrapper provided by (Remote?)
     * @return Returns an AuthenticatorTicketWrapper containing both the CST and Authenticator
     * @throws RejectedException Throws, if timestamps do not match
     * @TODO: Exception description
     */
    public AuthenticatorTicket handleSSResponse(byte[] SSSessionKey, AuthenticatorTicket lastWrapper, AuthenticatorTicket currentWrapper) throws RejectedException;
}
