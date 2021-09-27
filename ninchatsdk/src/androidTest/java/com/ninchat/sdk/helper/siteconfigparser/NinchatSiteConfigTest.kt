package com.ninchat.sdk.helper.siteconfigparser

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.json.JSONArray
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert

@RunWith(AndroidJUnit4::class)
class NinchatSiteConfigTest {
    val simpleSiteConfig = """
        {
          "description": "_COPY OF PRODUCTION SDK OMATERVEYS",
          "default": {
            "audienceRating": true,
            "audienceRealmId": "5lmphjc200m3g",
            "closeButton": true,
            "closeConfirmText": "Lopettaminen päättää käydyn keskustelun! Haluatko varmasti lopettaa?",
            "inQueueText": " ",
            "motd": " ",
            "noQueuesText": "Chat on suljettu.",
            "supportFiles": true,
            "supportVideo": true,
            "translations": {
              "Accept": "Hyväksy",
              "Audience in queue {{queue}} accepted.": "Keskustelu aloitettu.",
              "Close chat": "Sulje keskustelu",
              "Continue chat": "Jatka keskustelua",
              "Conversation ended": "Keskustelu on päättynyt. Voit sulkea chatin.",
              "Decline": "Hylkää",
              "End video call": "Lopeta videokeskustelu",
              "Enter your message": "Kirjoita viesti",
              "Good": "Hyvin",
              "How was our customer service?": "<strong>Miten asiointisi onnistui?</strong>",
              "Join audience queue {{audienceQueue.queue_attrs.name}}": "Aloita chat",
              "Join audience queue {{audienceQueue.queue_attrs.name}} (closed)": "Chat on suljettu",
              "Joined audience queue {{audienceQueue.queue_attrs.name}}, you are at position {{audienceQueue.queue_position}}.": "Pieni hetki. Olet jonossa sijalla {{audienceQueue.queue_position}}.",
              "Joined audience queue {{audienceQueue.queue_attrs.name}}, you are next.": "Pieni hetki. Olet seuraavana vuorossa.",
              "Next": "Jatka",
              "Okay": "Ok",
              "Poor": "Huonosti",
              "Skip": "Ohita",
              "Submit": "Lähetä",
              "Video call declined": "Videokeskustelu hylätty",
              "Video call answered": "Videokeskustelu aloitettu",
              "wants to video chat with you": "haluaa aloittaa videokeskustelun",
              "You are invited to a video call": "Lääkäri kutsui sinut videokeskusteluun. Klikkaa kamera-ikoni aloittaaksesi.",
              "You are invited to a video chat": "Sinut on kutsuttu videokeskusteluun"
            },
            "userAvatar": false,
            "userName": "Asiakas",
            "welcome": " "
          }
        }
    """.trimIndent()

    val siteConfigWithFiEnv = """
        {
          "description": "SDK dev queues",
          "default": {
            "agentAvatar": true,
            "audienceRating": true,
            "audienceRealmId": "5lmphjc200m3g",
            "audienceQueues": [
              "5lmpjrbl00m3g"
            ],
            "audienceAutoQueue": "5lmpjrbl00m3g",
            "noThanksButton": false,
            "closeButton": true,
            "closeConfirmText": "Lopettaminen päättää käydyn keskustelun! Haluatko varmasti lopettaa?",
            "language": "fi",
            "inQueueText": " ",
            "noQueuesText": " ",
            "motd": " ",
            "newUI": true,
            "sendButtonText": "Lähetä",
            "supportFiles": true,
            "supportVideo": true,
            "translations": {
              "Join audience queue {{audienceQueue.queue_attrs.name}}": "Aloita chat",
              "Join audience queue {{audienceQueue.queue_attrs.name}} (closed)": " ",
              "Joined audience queue {{audienceQueue.queue_attrs.name}}, you are at position {{audienceQueue.queue_position}}.": "Kiitos! Ohjaan sinut nyt hoitajan chat-vastaanotolle.<br><br>Olet jonossa sijalla {{audienceQueue.queue_position}}.<br>Odota, että Terveystalon asiantuntija poimii sinut jonosta.",
              "Audience in queue {{queue}} accepted.": "Keskustelu aloitettu.",
              "Joined audience queue {{audienceQueue.queue_attrs.name}}, you are next.": "Kiitos! Ohjaan sinut nyt hoitajan chat-vastaanotolle.<br>Olet seuraavana vuorossa.<br>Odota, että Terveystalon asiantuntija poimii sinut jonosta.",
              "Enter your message": "Kirjoita viesti",
              "Conversation ended": "Keskustelu on päättynyt. Voit sulkea chatin.",
              "Skip": "Ohita",
              "You are invited to a video call": "Lääkäri kutsui sinut videokeskusteluun. Klikkaa kamera-ikonia aloittaaksesi.",
              "Video call declined": "Videokeskustelu hylätty",
              "Video call answered": "Videokeskustelu aloitettu",
              "Toggle fullscreen": "Koko ruutu -tila on/off",
              "Toggle audio": "Ääni on/off",
              "Toggle microphone": "Mykistä mikrofoni",
              "Toggle video": "Video on/off",
              "End video call": "Lopeta videokeskustelu",
              "Add an emoji": "Lisää emoji",
              "How was our customer service?": "<strong>Miten asiointisi onnistui?</strong>",
              "Next": "Jatka",
              "Close window": "Sulje ikkuna",
              "Submit": "Lähetä",
              "Close chat": "Sulje keskustelu",
              "Continue chat": "Jatka keskustelua",
              "Good": "Hyvin",
              "Okay": "Ok",
              "Poor": "Huonosti",
              "Accept": "Hyväksy",
              "Decline": "Hylkää",
              "You are invited to a video chat": "Sinut on kutsuttu videokeskusteluun",
              "wants to video chat with you": "haluaa aloittaa videokeskustelun"
            },
            "userAvatar": false,
            "userName": "Asiakas",
            "welcome": "welcome-default"
          },
          "fi": {
            "welcome": "welcome-fi",
            "newUI": null,
            "preAudienceQuestionnaireStyle": "conversation",
            "questionnaireName": "Robotti",
            "audienceQueues": [
              "5lmpjrbl00m3g-audienceQ"
            ],
            "preAudienceQuestionnaire": [
              {
                "element": "textarea",
                "label": "Tervetuloa Mielen chattiin! Olen Terveystalon chat-robotti. Kysyn sinulta alkuun viisi kysymystä ja ohjaan sinut sitten keskustelemaan hoitajan kanssa. Kerrotko vähän, mitä sinulle kuuluu?",
                "name": "Syy",
                "buttons": {
                  "back": false
                },
                "redirects": [
                  {
                    "target": "SUD"
                  }
                ]
              },
              {
                "element": "radio",
                "label": "Hyvä, kiitos. Kun nyt ajattelet noita mielessäsi pyöriviä asioita, kerrotko kuinka häiritseviltä ne juuri nyt tuntuvat. Merkitse asteikolle häiritsevyyden määrä asteikolla nollasta kymmeneen, jolloin nolla tarkoittaa, että sinulla on neutraali ja rauhallinen olo ja kymmenen tarkoittaa, että asia häiritsee sinua todella kovasti.",
                "name": "SUD",
                "buttons": {
                  "back": false,
                  "next": false
                },
                "options": [
                  {
                    "value": "0",
                    "label": "0"
                  },
                  {
                    "value": "1",
                    "label": "1"
                  },
                  {
                    "value": "2",
                    "label": "2"
                  },
                  {
                    "value": "3",
                    "label": "3"
                  },
                  {
                    "value": "4",
                    "label": "4"
                  },
                  {
                    "value": "5",
                    "label": "5"
                  },
                  {
                    "value": "6",
                    "label": "6"
                  },
                  {
                    "value": "7",
                    "label": "7"
                  },
                  {
                    "value": "8",
                    "label": "8"
                  },
                  {
                    "value": "9",
                    "label": "9"
                  },
                  {
                    "value": "10",
                    "label": "10"
                  }
                ],
                "redirects": [
                  {
                    "target": "PHQ 1"
                  }
                ]
              },
              {
                "element": "radio",
                "label": "Seuraavat kaksi kysymystä kartoittavat vähän mielialaasi.Oletko viimeisen kahden viikon aikana ollut aiempaa vähemmän kiinnostunut tavallisten asioiden tekemisestä tai saanut niistä vain vähän (tai et juuri lainkaan) mielihyvän tunnetta?",
                "name": "PHQ 1",
                "buttons": {
                  "back": false,
                  "next": false
                },
                "options": [
                  {
                    "value": "0",
                    "label": "En lainkaan"
                  },
                  {
                    "value": "1",
                    "label": "Useina päivinä"
                  },
                  {
                    "value": "2",
                    "label": "Enemmän kuin puolet ajasta"
                  },
                  {
                    "value": "3",
                    "label": "Lähes joka päivä"
                  }
                ],
                "redirects": [
                  {
                    "target": "PHQ 2"
                  }
                ]
              },
              {
                "element": "radio",
                "label": "Oletko viimeisen kahden viikon aikana kokenut mielialasi alakuloiseksi, masentuneeksi tai toivottomaksi?",
                "name": "PHQ 2",
                "buttons": {
                  "back": false,
                  "next": false
                },
                "options": [
                  {
                    "value": "0",
                    "label": "En lainkaan"
                  },
                  {
                    "value": "1",
                    "label": "Useina päivinä"
                  },
                  {
                    "value": "2",
                    "label": "Enemmän kuin puolet ajasta"
                  },
                  {
                    "value": "3",
                    "label": "Lähes joka päivä"
                  }
                ],
                "redirects": [
                  {
                    "target": "Hyvät asiat"
                  }
                ]
              },
              {
                "element": "textarea",
                "label": "Kerrotko vielä, mitkä asiat ovat elämässäsi tällä hetkellä hyvin? Mistä saat hyvän mielen?",
                "name": "Hyvät asiat",
                "buttons": {
                  "back": false,
                  "next": "Aloita chat"
                }
              }
            ],
            "postAudienceQuestionnaire": [
              {
                "element": "textarea",
                "label": "Haluatko vielä jättää vapaata palautetta meille? Arvostamme kaikenlaista palautetta ja pyrimme kehittämään palvelua sen avulla.",
                "name": "Palaute",
                "buttons": {
                  "back": false,
                  "next": "Lähetä"
                }
              }
            ],
            "agentAvatar": false,
            "closeConfirmText": "Lopettaminen päättää käydyn keskustelun. Haluatko varmasti lopettaa?",
            "sendButtonText": null,
            "translations": {
              "Join audience queue {{audienceQueue.queue_attrs.name}}": "Aloitetaan",
              "Join audience queue {{audienceQueue.queue_attrs.name}} (closed)": "",
              "Joined audience queue {{audienceQueue.queue_attrs.name}}, you are at position {{audienceQueue.queue_position}}.": "Kiitos! Ohjaan sinut nyt hoitajan chat-vastaanotolle. <br><br>Odota hetki. Olet jonossa sijalla {{audienceQueue.queue_position}}.",
              "Audience in queue {{queue}} accepted.": "Keskustelu aloitettu. Mielen chatin hoitaja lukee viestisi ja vastaa kohta.",
              "Joined audience queue {{audienceQueue.queue_attrs.name}}, you are next.": "Kiitos! Ohjaan sinut nyt hoitajan chat-vastaanotolle. <br><br>Pieni hetki. Olet seuraavana vuorossa.",
              "Enter your message": "Kirjoita viesti",
              "Conversation ended": "Keskustelu on päättynyt. Voit sulkea chatin.",
              "Skip": "Ohita",
              "You are invited to a video call": "Lääkäri kutsui sinut videokeskusteluun. Klikkaa kamera-ikonia aloittaaksesi.",
              "Video call declined": "Videokeskustelu hylätty",
              "Video call answered": "Videokeskustelu aloitettu",
              "Toggle fullscreen": "Koko ruutu -tila on/off",
              "Toggle audio": "Ääni on/off",
              "Toggle microphone": "Mykistä mikrofoni",
              "Toggle video": "Video on/off",
              "End video call": "Lopeta videokeskustelu",
              "Add an emoji": "Lisää emoji",
              "How was our customer service?": "<strong>Kiitos! Miten asiointisi onnistui?</strong> Arvioi valitsemalla sopiva hymiö:",
              "Next": "Jatka"
            },
            "inQueueText": " ",
            "noQueuesText": " "
          }
        }
    """.trimIndent()

    @Test
    fun `site_config_should_be_null_initially`() {
        val ninchatSiteConfig = NinchatSiteConfig()
        Assert.assertEquals(null, ninchatSiteConfig.siteConfig)
    }

    @Test
    fun `should_be_able_to_set_empty_site_config`() {
        val ninchatSiteConfig = NinchatSiteConfig()
        ninchatSiteConfig.setConfigString(null)
        Assert.assertEquals(null, ninchatSiteConfig.siteConfig)
    }

    @Test
    fun `should_digest_exception_for_invalid_site_json`() {
        val ninchatSiteConfig = NinchatSiteConfig()
        ninchatSiteConfig.setConfigString("{key: invalid site config")
        Assert.assertEquals(null, ninchatSiteConfig.siteConfig)
    }

    @Test
    fun `should_parse_site_config_string_as_json`() {
        val ninchatSiteConfig = NinchatSiteConfig()
        ninchatSiteConfig.setConfigString(simpleSiteConfig)
        Assert.assertNotNull(null, ninchatSiteConfig.siteConfig)
    }

    @Test
    fun `should_return_default_for_null_environment`() {
        val environments = NinchatSiteConfig().sanitizePreferredEnvironments(null)
        Assert.assertEquals(arrayListOf("default"), environments)
    }

    @Test
    fun `should_add_default_is_not_present`() {
        val environments = NinchatSiteConfig().sanitizePreferredEnvironments(arrayListOf("fi"))
        Assert.assertEquals(arrayListOf("default", "fi"), environments)
    }

    @Test
    fun `should_not_add_default_if_already_present`() {
        val environments = NinchatSiteConfig().sanitizePreferredEnvironments(arrayListOf("default", "fi"))
        Assert.assertEquals(arrayListOf("default", "fi"), environments)
    }

    @Test
    fun `should_return_array_from_given_fi_environment`() {
        val ninchatSiteConfig = NinchatSiteConfig()
        ninchatSiteConfig.setConfigString(siteConfigWithFiEnv, arrayListOf("default", "fi"))

        val audienceQueue = ninchatSiteConfig.getArray("audienceQueues")

        Assert.assertEquals(JSONArray("""[
              "5lmpjrbl00m3g-audienceQ"
            ]""".trimIndent()), audienceQueue)
    }

    @Test
    fun `should_return_array_from_given_default_when_environment_not_found`() {
        val ninchatSiteConfig = NinchatSiteConfig()
        ninchatSiteConfig.setConfigString(siteConfigWithFiEnv, arrayListOf("noFoundFi"))

        val audienceQueue = ninchatSiteConfig.getArray("audienceQueues")

        Assert.assertEquals(JSONArray("""[
              "5lmpjrbl00m3g"
            ]""".trimIndent()), audienceQueue)
    }

    @Test
    fun `should_return_array_from_given_default_when_environment_not_provided`() {
        val ninchatSiteConfig = NinchatSiteConfig()
        ninchatSiteConfig.setConfigString(siteConfigWithFiEnv, null)

        val audienceQueue = ninchatSiteConfig.getArray("audienceQueues")
        Assert.assertEquals(JSONArray("""[
              "5lmpjrbl00m3g"
            ]""".trimIndent()), audienceQueue)
    }

    @Test
    fun `should_return_null_if_provided_key_not_found_in_environments`() {
        val ninchatSiteConfig = NinchatSiteConfig()
        ninchatSiteConfig.setConfigString(siteConfigWithFiEnv, arrayListOf("default", "fi"))

        val audienceQueue = ninchatSiteConfig.getArray("audienceQueuesNotFound")
        Assert.assertEquals(null, audienceQueue)
    }

    @Test
    fun `should_found_boolean_value_from_given_key_form_provided_environment`() {
        val ninchatSiteConfig = NinchatSiteConfig()
        ninchatSiteConfig.setConfigString(siteConfigWithFiEnv, arrayListOf("default", "fi"))

        val audienceQueue = ninchatSiteConfig.getBoolean("agentAvatar")
        Assert.assertEquals(false, audienceQueue)
    }

    @Test
    fun `should_found_value_from_default_environment_if_provided_environment_not_found`() {
        val ninchatSiteConfig = NinchatSiteConfig()
        ninchatSiteConfig.setConfigString(siteConfigWithFiEnv, arrayListOf("default", "fi"))

        val audienceQueue = ninchatSiteConfig.getBoolean("audienceRating")
        Assert.assertEquals(true, audienceQueue)
    }

    @Test
    fun `should_found_value_from_given_key_from_default_environment_if_provided_environment_null`() {
        val ninchatSiteConfig = NinchatSiteConfig()
        ninchatSiteConfig.setConfigString(siteConfigWithFiEnv, arrayListOf("default", "fi"))

        val newUI = ninchatSiteConfig.getBoolean("newUI")
        Assert.assertEquals(false, newUI)
    }

    @Test
    fun `should_return_false_if_key_not_found`() {
        val ninchatSiteConfig = NinchatSiteConfig()
        ninchatSiteConfig.setConfigString(siteConfigWithFiEnv, arrayListOf("default", "fi"))

        val newUI = ninchatSiteConfig.getBoolean("newUINoKey")
        Assert.assertEquals(false, newUI)
    }

    @Test
    fun `should_found_string_value_from_given_key_form_provided_environment`() {
        val ninchatSiteConfig = NinchatSiteConfig()
        ninchatSiteConfig.setConfigString(siteConfigWithFiEnv, arrayListOf("default", "fi"))

        val value = ninchatSiteConfig.getString("welcome")
        Assert.assertEquals("welcome-fi", value)
    }

    @Test
    fun `should_found_string_value_from_default_environment_if_provided_environment_not_found`() {
        val ninchatSiteConfig = NinchatSiteConfig()
        ninchatSiteConfig.setConfigString(siteConfigWithFiEnv, arrayListOf("default", "fi"))

        val value = ninchatSiteConfig.getString("userName")
        Assert.assertEquals("Asiakas", value)
    }

    @Test
    fun `should_return_null_object_where_the_value_is_null`() {
        val ninchatSiteConfig = NinchatSiteConfig()
        ninchatSiteConfig.setConfigString(siteConfigWithFiEnv, arrayListOf("default", "fi"))

        val value = ninchatSiteConfig.getString("sendButtonText")
        Assert.assertEquals(null, value)
    }

    @Test
    fun `should_return_null_if_key_not_found`() {
        val ninchatSiteConfig = NinchatSiteConfig()
        ninchatSiteConfig.setConfigString(siteConfigWithFiEnv, arrayListOf("default", "fi"))

        val value = ninchatSiteConfig.getString("sendButtonText_NoKey")
        Assert.assertEquals(null, value)
    }

    @Test
    fun `should_get_agent_avatar_true`() {
        val siteConfigWithAgentAvatarSetFalse = """{
          "description": "SDK dev queues",
          "default": {
            "agentAvatar": true,
            "audienceRating": true,
            "audienceRealmId": "5lmphjc200m3g",
            "audienceQueues": [
              "5lmpjrbl00m3g"
            ]
          }
        }""".trimIndent()
        val ninchatSiteConfig = NinchatSiteConfig()
        ninchatSiteConfig.setConfigString(siteConfigWithAgentAvatarSetFalse, arrayListOf("default", "fi"))

        val value = ninchatSiteConfig.showAgentAvatar()
        Assert.assertEquals(true, value)
    }

    @Test
    fun `should_get_agent_avatar_false_for_a_url`() {
        val siteConfigWithAgentAvatarSetFalse = """{
          "description": "SDK dev queues",
          "default": {
            "agentAvatar": "something else",
            "audienceRating": true,
            "audienceRealmId": "5lmphjc200m3g",
            "audienceQueues": [
              "5lmpjrbl00m3g"
            ]
          }
        }""".trimIndent()
        // "" -> false
        // "false" -> false
        // false -> false
        // "null" -> false
        // null -> false
        val ninchatSiteConfig = NinchatSiteConfig()
        ninchatSiteConfig.setConfigString(siteConfigWithAgentAvatarSetFalse, arrayListOf("default", "fi"))

        val value = ninchatSiteConfig.showAgentAvatar(fallback = false)
        Assert.assertEquals(false, value)
    }
}