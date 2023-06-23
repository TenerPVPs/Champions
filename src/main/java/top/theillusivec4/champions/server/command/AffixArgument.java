package top.theillusivec4.champions.server.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import top.theillusivec4.champions.Champions;
import top.theillusivec4.champions.api.IAffix;
import top.theillusivec4.champions.server.command.AffixArgument.IAffixProvider;

public class AffixArgument implements ArgumentType<IAffixProvider> {

  private static final Collection<String> EXAMPLES = Arrays.asList("molten", "reflecting");
  private static final DynamicCommandExceptionType UNKNOWN_AFFIX = new DynamicCommandExceptionType(
      type -> Component.translatable("argument.champions.affix.unknown", type));

  public static AffixArgument affix() {
    return new AffixArgument();
  }

  public static Collection<IAffix> getAffixes(CommandContext<CommandSourceStack> context,
                                              String name)
      throws CommandSyntaxException {
    return context.getArgument(name, IAffixProvider.class).getAffixes(context.getSource());
  }

  @Override
  public Collection<String> getExamples() {
    return EXAMPLES;
  }

  @Override
  public IAffixProvider parse(StringReader reader) throws CommandSyntaxException {
    String s = reader.getString().substring(reader.getCursor(), reader.getTotalLength());
    String[] split = s.split(" ");
    List<IAffix> affixes = new ArrayList<>();

    for (String id : split) {
      affixes.add(Champions.API.getAffix(id).orElseThrow(() -> UNKNOWN_AFFIX.create(id)));
    }

    while (reader.canRead()) {
      reader.skip();
    }
    return (source) -> affixes;
  }

  @FunctionalInterface
  public interface IAffixProvider {

    Collection<IAffix> getAffixes(CommandSourceStack source) throws CommandSyntaxException;
  }
}
