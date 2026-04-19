package the.monopoly.game.test.fixtures.validators;

import org.springframework.stereotype.Service;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.test.fixtures.repository.PlayerRepository;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@Service
public class PlayerValidator {
  private final PlayerRepository playerRepository;
  private final NormalisationUtils normaliser;

  public PlayerValidator(PlayerRepository playerRepository, NormalisationUtils normaliser) {
    this.playerRepository = playerRepository;
    this.normaliser = normaliser;
  }

  public void assertPawnsAtPlay(List<Player.ID> expectations, Locale locale) {
    assertThat(playerRepository.all()
        .map(Player::id)
        .map(it -> normaliser.normalise(it, locale))
        .sorted(Player.ID.Comparators.natural()))
        .containsExactlyElementsOf(expectations);
  }
}
