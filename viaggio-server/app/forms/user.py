from wtforms import Form, StringField, IntegerField
from wtforms import ValidationError
from wtforms.validators import DataRequired, length, Email, EqualTo
from ..models import User


class RegistrationForm(Form):
    email = StringField('Email', validators=[DataRequired(), length(1, 64), Email()])
    name = StringField('Name', validators=[DataRequired(), length(1, 64)])
    passwordHash = StringField('Password', validators=[DataRequired(), length(1, 128),
                                                        EqualTo('passwordHash2')])
    passwordHash2 = StringField('Password2', validators=[DataRequired(), length(1, 128)])
    profileImageName = StringField('ProfileImageName', validators=[length(0, 64)])

    def validate_email(self, field):
        if User.query.filter_by(email=field.data).first():
            raise ValidationError('Email already registered.')


class ChangePasswordForm(Form):
    oldPasswordHash = StringField('OldPwd', validators=[DataRequired(), length(1, 128)])
    passwordHash = StringField('Password', validators=[DataRequired(), length(1, 128),
                                                        EqualTo('passwordHash2')])
    passwordHash2 = StringField('Password2', validators=[DataRequired(), length(1, 128)])


class ChangeUserNameForm(Form):
    name = StringField('Name', validators=[DataRequired(), length(1, 64)])
