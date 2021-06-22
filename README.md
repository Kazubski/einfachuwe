Bester Stream! https://www.twitch.tv/einfachuwe42

Application bei twitch dev anmelden client_id client_secret besorgen

QAuthToken erstellen mit folgenden Scopes https://twitchapps.com/tokengen/

channel:read:subscriptions user:read:blocked_users user:edit:follows user:edit:broadcast chat:edit chat:read whispers:read whispers:edit channel:moderate moderation:read channel:manage:broadcast


```
move /src/main/resources/twitch2.txt
to /src/main/resources/twitch.txt
```

/src/main/resources/twitch.txt
```
IDENTITY_PROVIDER=twitch
O_AUTH=oauth:
CLIENT_ID=
CLIENT_SECRET=
CHANNEL_NAME=
```
