package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADMISSION_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DOB;
import static seedu.address.logic.parser.CliSyntax.PREFIX_IC;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REMARK;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CliSyntax.PREFIX_WARD;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.CollectionUtil;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.AdmissionDate;
import seedu.address.model.person.Dob;
import seedu.address.model.person.Ic;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;
import seedu.address.model.person.Ward;
import seedu.address.model.tag.Tag;

/**
 * Edits the details of an existing person in the address book.
 */
public class EditCommand extends Command {

    public static final String COMMAND_WORD = "edit";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits the details of the person identified "
            + "by the index number used in the displayed person list. "
            + "Existing values will be overwritten by the input values.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + "[" + PREFIX_NAME + "NAME] "
            + "[" + PREFIX_IC + "I/C] "
            + "[" + PREFIX_DOB + "DATE OF BIRTH] "
            + "[" + PREFIX_WARD + "WARD] "
            + "[" + PREFIX_ADMISSION_DATE + "ADMISSION DATE] "
            + "[" + PREFIX_TAG + "TAG]...\n"
            + "[" + PREFIX_REMARK + "REMARK]...\n"
            + "Example: " + COMMAND_WORD + " 1 "
            + PREFIX_IC + "T1234567Q "
            + PREFIX_DOB + "12/08/1999";

    public static final String MESSAGE_EDIT_PERSON_SUCCESS = "Edited Person: %1$s";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";
    public static final String MESSAGE_DUPLICATE_PERSON = "This person already exists in the address book.";

    public static final String MESSAGE_INDEX_LESS_THAN_ONE = "Index should not be less than 1.";

    private final Index index;
    private final EditPersonDescriptor editPersonDescriptor;

    /**
     * @param index of the person in the filtered person list to edit
     * @param editPersonDescriptor details to edit the person with
     */
    public EditCommand(Index index, EditPersonDescriptor editPersonDescriptor) {
        requireNonNull(index);
        requireNonNull(editPersonDescriptor);

        this.index = index;
        this.editPersonDescriptor = new EditPersonDescriptor(editPersonDescriptor);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (index.getZeroBased() >= lastShownList.size() || index.getZeroBased() == 0) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToEdit = lastShownList.get(index.getZeroBased());
        Person editedPerson = createEditedPerson(personToEdit, editPersonDescriptor);

        if (!personToEdit.isSamePerson(editedPerson) && model.hasPerson(editedPerson)) {
            throw new CommandException(MESSAGE_DUPLICATE_PERSON);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDate dobToCheck = LocalDate.parse(editedPerson.getDob().value, formatter);
        LocalDate adToCheck = LocalDate.parse(editedPerson.getAdmissionDate().value, formatter);
        if (dobToCheck.isAfter(adToCheck)) {
            throw new CommandException(Messages.MESSAGE_DOB_LATER_THAN_ADMISSION);
        }

        model.setPerson(personToEdit, editedPerson);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        return new CommandResult(String.format(MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson)));
    }

    /**
     * Creates and returns a {@code Person} with the details of {@code personToEdit}
     * edited with {@code editPersonDescriptor}.
     */
    private static Person createEditedPerson(Person personToEdit, EditPersonDescriptor editPersonDescriptor) {
        assert personToEdit != null;

        Name updatedName = editPersonDescriptor.getName().orElse(personToEdit.getName());
        Set<Tag> updatedTags = editPersonDescriptor.getTags().orElse(personToEdit.getTags());
        Ic updatedIc = editPersonDescriptor.getIc().orElse(personToEdit.getIc());
        Dob updatedDob = editPersonDescriptor.getDob().orElse(personToEdit.getDob());
        AdmissionDate updatedAdmissionDate =
                editPersonDescriptor.getAdmissionDate().orElse(personToEdit.getAdmissionDate());
        Ward updatedWard = editPersonDescriptor.getWard().orElse(personToEdit.getWard());
        Remark updatedRemark = editPersonDescriptor.getRemark().orElse(personToEdit.getRemark());

        return new Person(updatedName, updatedTags, updatedDob, updatedIc, updatedAdmissionDate,
                updatedWard, updatedRemark);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EditCommand)) {
            return false;
        }

        EditCommand otherEditCommand = (EditCommand) other;
        return index.equals(otherEditCommand.index)
                && editPersonDescriptor.equals(otherEditCommand.editPersonDescriptor);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("editPersonDescriptor", editPersonDescriptor)
                .toString();
    }

    /**
     * Stores the details to edit the person with. Each non-empty field value will replace the
     * corresponding field value of the person.
     */
    public static class EditPersonDescriptor {
        private Name name;
        private Set<Tag> tags;
        private Ic ic;
        private Dob dob;
        private AdmissionDate admissionDate;
        private Ward ward;
        private Remark remark;
        public EditPersonDescriptor() {}

        /**
         * Copy constructor.
         * A defensive copy of {@code tags} is used internally.
         */
        public EditPersonDescriptor(EditPersonDescriptor toCopy) {
            setName(toCopy.name);
            setTags(toCopy.tags);
            setDob(toCopy.dob);
            setIc(toCopy.ic);
            setAdmissionDate(toCopy.admissionDate);
            setWard(toCopy.ward);
            setRemark(toCopy.remark);
        }

        /**
         * Returns true if at least one field is edited.
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(name, ic, dob, admissionDate, ward, tags);
        }

        public void setName(Name name) {
            this.name = name;
        }

        public Optional<Name> getName() {
            return Optional.ofNullable(name);
        }

        /**
         * Sets {@code tags} to this object's {@code tags}.
         * A defensive copy of {@code tags} is used internally.
         */
        public void setTags(Set<Tag> tags) {
            this.tags = (tags != null) ? new HashSet<>(tags) : null;
        }

        /**
         * Returns an unmodifiable tag set, which throws {@code UnsupportedOperationException}
         * if modification is attempted.
         * Returns {@code Optional#empty()} if {@code tags} is null.
         */
        public Optional<Set<Tag>> getTags() {
            return (tags != null) ? Optional.of(Collections.unmodifiableSet(tags)) : Optional.empty();
        }
        public void setIc(Ic ic) {
            this.ic = ic;
        }
        public Optional<Ic> getIc() {
            return Optional.ofNullable(ic);
        }
        public void setDob(Dob dob) {
            this.dob = dob;
        }
        public Optional<Dob> getDob() {
            return Optional.ofNullable(dob);
        }
        public void setAdmissionDate(AdmissionDate admissionDate) {
            this.admissionDate = admissionDate;
        }
        public Optional<AdmissionDate> getAdmissionDate() {
            return Optional.ofNullable(admissionDate);
        }
        public void setWard(Ward ward) {
            this.ward = ward;
        }
        public Optional<Ward> getWard() {
            return Optional.ofNullable(ward);
        }
        public void setRemark(Remark remark) {
            this.remark = remark;
        }
        public Optional<Remark> getRemark() {
            return Optional.ofNullable(remark);
        }
        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }

            // instanceof handles nulls
            if (!(other instanceof EditPersonDescriptor)) {
                return false;
            }

            EditPersonDescriptor otherEditPersonDescriptor = (EditPersonDescriptor) other;
            return Objects.equals(name, otherEditPersonDescriptor.name)
                    && Objects.equals(tags, otherEditPersonDescriptor.tags)
                    && Objects.equals(ic, otherEditPersonDescriptor.ic)
                    && Objects.equals(dob, otherEditPersonDescriptor.dob)
                    && Objects.equals(admissionDate, otherEditPersonDescriptor.admissionDate)
                    && Objects.equals(ward, otherEditPersonDescriptor.ward)
                    && Objects.equals(remark, otherEditPersonDescriptor.remark);

        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .add("name", name)
                    .add("dob", dob)
                    .add("ic", ic)
                    .add("tags", tags)
                    .add("admissionDate", admissionDate)
                    .add("ward", ward)
                    .add("remark", remark)
                    .toString();
        }
    }
}
