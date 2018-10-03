# Responscodes

### Globaal

Als een endpoint niet staat beschreven in deze gids, gebruik de standaard responscodes:

| Code | Omschrijving |
| ---- | ------------ |
|   1  | True |
|   0  | False |
|  -1  | Niet genoeg argumenten |
|  -2  | Argumenten onjuist geformatteerd |

### /auth/login
| Code | Omschrijving |
| ---- | ------------ |
|  >1  | Sessie-token |
|  -1  | Niet genoeg argumenten |
|  -2  | Argumenten onjuist geformatteerd |
|  -3  | Gebruiker is niet geregistreerd |
|  -4  | Wachtwoord incorrect |

### /auth/register
| Code | Omschrijving |
| ---- | ------------ |
|   1  | Success |
|  -1  | Niet genoeg argumenten |
|  -2  | Argumenten onjuist geformatteerd |
|  -5  | Gebruiker bestaat al |
|  -6  | Gebruiker bestaat al maar heeft telefoonnummer nog niet geverifieerd |
|  -7  | Interne fout m.b.t. SMS-verificatie |

### /auth/verify
| Code | Omschrijving |
| ---- | ------------ |
|   1  | Success |
|   0  | Ongeldige verificatietoken of kan gebruiker niet vinden |
|  -1  | Niet genoeg argumenten |
|  -2  | Argumenten onjuist geformatteerd |

#### /auth/sync
| Code | Omschrijving |
| ---- | ------------ |
|   1  | Sessie-token is geldig |
|   0  | Sessie-token is niet geldig |
|  -1  | Niet genoeg argumenten |
|  -2  | Argumenten onjuist geformatteerd |