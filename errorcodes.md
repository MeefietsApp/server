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
|  >1  | Sessie-token |
|  -1  | Niet genoeg argumenten |
|  -2  | Argumenten onjuist geformatteerd |
|  -3  | Gebruiker is niet geregistreerd |
|  -4  | Wachtwoord incorrect |

### /auth/register
|   1  | Success |
|  -1  | Niet genoeg argumenten |
|  -2  | Argumenten onjuist geformatteerd |
|  -5  | Gebruiker bestaat al |

#### /auth/sync
|   1  | Sessie-token is geldig |
|   0  | Sessie-token is niet geldig |
|  -1  | Niet genoeg argumenten |
|  -2  | Argumenten onjuist geformatteerd |