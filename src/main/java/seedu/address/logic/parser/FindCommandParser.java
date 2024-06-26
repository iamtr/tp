package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_IC;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;

import java.util.Arrays;
import java.util.Collections;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.IcContainsKeywordPredicate;
import seedu.address.model.person.NameContainsKeywordsPredicate;

/**
 * Parses input arguments and creates a new FindCommand object
 */
public class FindCommandParser implements Parser<FindCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the FindCommand
     * and returns a FindCommand object for execution.
     * @throws ParseException if the user input does not conform the expected format
     */
    public FindCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }
        assert trimmedArgs.length() > 1;

        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_IC);

        if (!argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_IC);

        if (!hasOneParamOnly(argMultimap)) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }

        if (argMultimap.getValue(PREFIX_NAME).isPresent()) {
            String keywords = argMultimap.getValue(PREFIX_NAME).get();
            if (keywords.equals("")) {
                return new FindCommand(new NameContainsKeywordsPredicate(Collections.emptyList()));
            } else {
                String[] nameKeywords = keywords.split("\\s+");
                return new FindCommand(new NameContainsKeywordsPredicate(Arrays.asList(nameKeywords)));
            }
        } else if (argMultimap.getValue(PREFIX_IC).isPresent()) {
            String keyword = argMultimap.getValue(PREFIX_IC).get();
            if (keyword.split("\\s+").length != 1) {
                throw new ParseException(
                        String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
            }
            return new FindCommand(new IcContainsKeywordPredicate(keyword));
        } else {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }
    }

    /**
     * Check if only 1 parameter is used.
     * @param argMultimap Input being mapped.
     * @return true if only name or ic input.
     */
    private boolean hasOneParamOnly(ArgumentMultimap argMultimap) {
        boolean hasNameOnly =
                argMultimap.getValue(PREFIX_NAME).isPresent() && !argMultimap.getValue(PREFIX_IC).isPresent();
        boolean hasIcOnly =
                argMultimap.getValue(PREFIX_IC).isPresent() && !argMultimap.getValue(PREFIX_NAME).isPresent();
        return hasIcOnly || hasNameOnly;
    }
}
