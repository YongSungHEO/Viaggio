from flask import jsonify, request
from . import api
from .. import db
from ..models import User
from ..forms.user import RegistrationForm, ChangePasswordForm, ChangeUserInfoForm, LoginForm
from .errors import bad_request
import uuid


@api.route('/users', methods=['POST'])
def create_user():
    form = RegistrationForm(request.form)
    if form.validate():
        user = User(email=request.form['email'],
                    name=request.form['name'],
                    passwordHash=request.form['passwordHash'])
        db.session.add(user)
        db.session.commit()
        return jsonify({ 'email': user.email, 'name': user.name, 'token': user.token }), 200

    if form.passwordHash.errors:
        return jsonify({
            'message': 400,
            'detail': form.passwordHash.errors[0]
        }), 400

    if form.email.errors:
        return jsonify({
            'message': 401,
            'detail': form.email.errors[0]
        }), 400

    return jsonify({
        'message': 402,
        'detail': 'User vaildation is failed'
    }), 400


@api.route('/users/changepwd', methods=['POST'])
def change_password():
    form = ChangePasswordForm(request.form)
    if form.validate():
        user = User.query.filter_by(token=request.headers['authorization'],
                                    passwordHash=request.form['oldPasswordHash']).first()
        if user is None:
            return jsonify({
                'message': 401,
                'detail': 'There is no user matched with token and pwd when change user pwd.'
            }), 400
        user.passwordHash = request.form['passwordHash']
        db.session.add(user)
        db.session.commit()
        return jsonify({ 'result': 'User password is changed.' }), 200
    
    if form.passwordHash.errors:
        return jsonify({
            'message': 402,
            'detail': 'When change pwd, validation error is occurred.'
        }), 400

    return jsonify({
        'message': 403,
        'detail': 'Change password validation is failed.'
    }), 400


@api.route('/users/changeinfo', methods=['POST'])
def change_name():
    form = ChangeUserInfoForm(request.form)
    if form.validate():
        user = User.query.filter_by(token=request.headers['authorization']).first()
        if user is None:
            return jsonify({
                'message': 401,
                'detail': 'There is no user matched with token when change user name.'
            }), 400
        user.name = request.form['name']
        if request.form.get('profileImageName') is not None \
                and request.form.get('profileImageUrl') is not None:
            user.profileImageName = request.form['profileImageName']
            user.profileImageUrl = request.form['profileImageUrl']
        db.session.add(user)
        db.session.commit()
        return jsonify({ 'result': 'User info is changed.' }), 200
    
    return jsonify({
        'message': 400,
        'detail': 'When change user info, validation error is occurred.'
    }), 400


@api.route('/users/login', methods=['POST'])
def login():
    form = LoginForm(request.form)
    if form.validate():
        user = User.query.filter_by(email=request.form['email'],
                                    passwordHash=request.form['passwordHash']).first()
        if user is None:
            return jsonify({
                'message': 401,
                'detail': 'There is no user matched with pwd.'
            }), 400

        if user.token:
            return jsonify({ 'token': user.token }), 200
        else:
            user.token = str(uuid.uuid4())
            db.session.add(user)
            db.session.commit()
            return jsonify({ 'token': user.token }), 200

    return jsonify({
        'message': 400,
        'detail': 'Login validation is failed.'
    }), 400


@api.route('/users/logout')
def logout():
    user = User.query.filter_by(token=request.headers['authorization']).first()
    if user is None:
        return jsonify({ 'result': 'Token is already null.' }), 200

    user.token = None
    db.session.add(user)
    db.session.commit()
    return jsonify({ 'result': 'User logout is success.' }), 200
