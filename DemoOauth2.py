#!/usr/bin/env python
# -*- coding:utf-8 -*-
# Author: Hang
# Date: 2021/7/28

from flask import Flask
from flask import jsonify, request, redirect
from furl import furl

import requests
import json

app = Flask(__name__)

# Get from the Oauth2 service
CLIENT_ID = "<your_client_id>" 
CLIENT_SECRET = "<yout_client_secret>"

PORTAL_URL = "https://<your_oauth_server_url>"
OAUTH2_AUTHORIZE_URL = PORTAL_URL + "/api/oauth2/authorize"
OAUTH2_TOKEN_URL = PORTAL_URL + "/api/oauth2/token"
OAUTH2_USERINFO_URL = PORTAL_URL + "/api/oauth2/userinfo"

# This url need to be configured in the application's callback url
REDIRECT_URL = "<your_redirect_url>" 


def gettenant_access_token(code):
    headers = {'Accept': 'application/json'}
    data = {
        'grant_type': 'authorization_code',
        'client_id': CLIENT_ID,
        'client_secret': CLIENT_SECRET,
        'code': code,
        'redirect_uri': REDIRECT_URL
    }
    response = requests.post(url=OAUTH2_TOKEN_URL, json=data, headers=headers)
    access_token = json.loads(response.text).get('access_token')
    return access_token


def getuserinfo(access_token):
    headers = {
        'accept': 'application/json',
        'Authorization': 'Bearer {}'.format(access_token)
    }
    # Read the user info with token
    response = requests.get(OAUTH2_USERINFO_URL, headers=headers)
    return json.loads(response.text)


# This is the address of the application you want to access. 
# When the browser accesses it, it automatically requests the Oauth2 server for the code and then redirect to the callback url
@app.route('/', methods=['GET', 'POST'])
def prelogin():
    params = {
        'response_type': 'code',
        'client_id': CLIENT_ID,
        'redirect_uri': REDIRECT_URL
    }

    url = furl(OAUTH2_AUTHORIZE_URL).add(params).url
    return redirect(url, 302)


# This is the url to receive the login information. In this Demo, it is only used to print the login information.
@app.route('/home')
def oauth2_callback():
    code = request.args.get('code')
    access_token = gettenant_access_token(code)
    userinfo = getuserinfo(access_token)
    return jsonify({
        'status': 'success',
        'data': userinfo
    })


if __name__ == '__main__':
    app.run()
