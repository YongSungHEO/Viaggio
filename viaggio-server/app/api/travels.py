from flask import jsonify, request
from . import api
from .. import db
from ..models import Travel, TravelCard
from ..forms.travel import CreateTravelForm, UpdateTravelForm
from ..errors import bad_request


@api.route('/my/travels', methods=['POST'])
def create_travel():
    form = CreateTravelForm(request.form)
    if form.validate():
        entireCountries = [request.form.get('entireCountry')]
        travel = Travel(userId=request.user.id,
                        startDate=request.form.get('startDate'),
                        endDate=request.form.get('endDate'),
                        travelType=request.form.get('travelType'),
                        entireCountry=entireCountries,
                        title=request.form.get('title'),
                        thema=request.form.get('thema'),
                        backgroundImageName=request.form.get('backgroundImageName'),
                        backgroundImageUrl=request.form.get('backgroundImageUrl'),
                        share=request.form.get('share'),
                        isDelete=request.form.get('isDelete'))
        db.session.add(travel)
        db.session.commit()
        return jsonify({ 'travel': travel.as_dict() }), 200

    if form.startDate.errors:
        return bad_request(401, 'StartDate validation error.')

    if form.travelType.errors:
        return bad_request(402, form.travelType.errors[0])

    if form.entireCountry.errors:
        return bad_request(403, 'Country validation error.')
    
    return bad_request(400, 'CreateTravelForm validation error.')


@api.route('/my/travels')
def get_travels():
    travels = Travel.query.filter_by(userId=request.user.id)
    return jsonify({
        'travels': [travel.to_json() for travel in travels]
    }), 200


@api.route('my/travels/<int:id>')
def get_specific_travel(id):
    travel = Travel.query.filter_by(userId=request.user.id, id=id).first_or_404()
    return jsonify({
        'travel': travel.as_dict()
    }), 200


@api.route('/my/travels/<int:id>', methods=['PUT'])
def update_travel(id):
    form = UpdateTravelForm(request.form)
    if form.validate():
        travel = Travel.query.get_or_404(id)
        if request.form.get('title') is not None:
            travel.title = request.form.get('title')
        if request.form.get('travelType') and request.form.get('travelType') != travel.travelType:
            travel.travelType = request.form.get('travelType')
        if request.form.get('addCountry') is not None \
            and request.form.get('addCountry') not in travel.entireCountry:
            tempData = list(travel.entireCountry)
            tempData.append(request.form.get('addCountry'))
            travel.entireCountry = tempData
        travel.endDate = request.form.get('endDate')
        if request.form.get('addThema') is not None \
            and request.form.get('addThema') not in travel.thema:
            tempThema = list(trave.thema)
            tempThema.append(request.form.get('addThema'))
            travel.thema = tempThema
        travel.backgroundImageName = request.form.get('backgroundImageName')
        travel.backgroundImageUrl = request.form.get('backgroundImageUrl')
        if request.form.get('share'):
            travel.share = True
        else:
            travel.share = False
        db.session.commit()
        return jsonify({ 'travel': travel.as_dict() })

    if form.travelType.errors:
        return bad_request(401, 'TravelType validation error.')
    if form.endDate.errors:
        return bad_request(402, 'EndDate validation error.')


@api.route('/my/travels/<int:id>', methods=['DELETE'])
def delete_travel(id):
    travel = Travel.query.get_or_404(id)
    travel.isDelete = True
    db.session.add(travel)
    db.session.commit()
    return jsonify({ 'result': 'Travel is archived.' })
