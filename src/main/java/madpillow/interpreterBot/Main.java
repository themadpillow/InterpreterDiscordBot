package madpillow.interpreterBot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserSpeakingEvent;
import sx.blah.discord.handle.obj.IVoiceChannel;

public class Main {
	private static final String TOKEN = Config.getToken();
	private static IDiscordClient client;
	private static String PREFIX = "/";

	public static void main(String[] args) {
		Main app = new Main();
		client = login(TOKEN);
		client.getDispatcher().registerListener(app);
	}

	public static IDiscordClient login(String token) {
		ClientBuilder clientBuilder = new ClientBuilder();
		clientBuilder.withToken(token).build();
		return clientBuilder.login();
	}

	private void CommandHandler(String cmd, List<String> args, MessageEvent event) {
		if (cmd.equalsIgnoreCase("join")) {
			if (client.getConnectedVoiceChannels().size() != 0) {
				return;
			}

			IVoiceChannel userVoiceChannel = event.getAuthor().getVoiceStateForGuild(event.getGuild()).getChannel();
			if (userVoiceChannel == null) {
				return;
			}

			userVoiceChannel.join();
		} else if (cmd.equalsIgnoreCase("leave")) {
			if (client.getConnectedVoiceChannels().size() == 0) {
				return;
			}

			IVoiceChannel botVoiceChannel = client.getConnectedVoiceChannels().get(0);
			botVoiceChannel.leave();

		}
	}

	@EventSubscriber
	public void onMessage(MessageReceivedEvent event) {
		System.out.println(event.getMessage().getContent());
		if (event.getMessage().getContent().startsWith(PREFIX)) {
			String[] args = event.getMessage().getContent().split(" ");
			String cmd = args[0].substring(1);
			List<String> argsList = new ArrayList<String>(Arrays.asList(args));
			argsList.remove(0);

			CommandHandler(cmd, argsList, event);
		}
	}

	@EventSubscriber
	public void onSpeaking(UserSpeakingEvent event) {
		if (event.isSpeaking()) {
			//Discord4Jのバグにより通話に参加できずReceiveが呼び出されない
			event.getGuild().getAudioManager().subscribeReceiver((audio, user, sequence, timestamp) -> {
				System.out.println(user.getName());
			});
		}
	}
}