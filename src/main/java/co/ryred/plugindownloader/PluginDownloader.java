/*
 * http://ryred.co/
 * ace[at]ac3-servers.eu
 *
 * =================================================================
 *
 * Copyright (c) 2016, Cory Redmond
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *  Neither the name of PluginDownloader nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package co.ryred.plugindownloader;

import be.maximvdw.spigotsite.SpigotSiteCore;
import be.maximvdw.spigotsite.api.SpigotSite;
import be.maximvdw.spigotsite.api.SpigotSiteAPI;
import be.maximvdw.spigotsite.api.resource.PremiumResource;
import be.maximvdw.spigotsite.api.resource.Resource;
import be.maximvdw.spigotsite.api.user.User;
import be.maximvdw.spigotsite.api.user.exceptions.InvalidCredentialsException;
import be.maximvdw.spigotsite.user.SpigotUser;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.File;

/**
 * Created by Cory Redmond on 11/04/2016.
 *
 * @author Cory Redmond <ace@ac3-servers.eu>
 */
public class PluginDownloader {

	public static void main( String... args ) {

		OptionParser optionParser = new OptionParser();

		OptionSpec<Integer> resourceId = optionParser.accepts( "rid" ).withRequiredArg().describedAs( "The ID of the resource" ).ofType( Integer.class );
		OptionSpec<String> username = optionParser.accepts( "username" ).withRequiredArg().describedAs( "Your username." ).ofType( String.class );
		OptionSpec<String> password = optionParser.accepts( "password" ).withRequiredArg().describedAs( "Your password." ).ofType( String.class );
		OptionSpec<String> filename = optionParser.accepts( "filename" ).withRequiredArg().describedAs( "The filename to save to." ).ofType( String.class );

		OptionSet options = optionParser.parse( args );

		if( !options.has( resourceId ) || resourceId.value( options ) < 0 ) {
			System.err.println( "You need to supply a valid resource id!" );
			System.exit( 1 );
		} else if ( !options.has( filename ) ) {
			System.err.println( "You need to supply a valid filename to save to!" );
			System.exit( 1 );
		} else if( options.has( username ) || options.has( password ) ) {
			System.err.println( "If you're supplying a username/password you need both!" );
			System.exit( 1 );
		}

		new SpigotSiteCore();

		User user = new SpigotUser( "Anon" );
		if( options.has( username ) && options.has( password ) ) {
			try {
				user = SpigotSite.getAPI().getUserManager().authenticate( username.value( options ), password.value( options ) );
			} catch ( InvalidCredentialsException e ) {
				System.err.println( "Unable to authenticate! Check your credentials, and ensure 2fa is disabled." );
				System.exit( 1 );
			}
		}

		Resource resource = SpigotSite.getAPI().getResourceManager().getResourceById( resourceId.value( options ) );

		if( resource instanceof PremiumResource && !user.isAuthenticated() ) {
			System.err.println( "That resource is premium and you're not authenticated!" );
			System.exit( 1 );
		}

		File saveFile = new File( filename.value( options ) );

		if( saveFile.exists() ) {
			saveFile.delete();
		} else {
			saveFile.getParentFile().mkdirs();
		}

		System.out.println( "Saving \"" + resource.getResourceName() + "\" by " + resource.getAuthor().getUsername() );
		System.out.println( "\t to file: " + saveFile.getPath() );

		resource.downloadResource( user, saveFile );

	}

}
